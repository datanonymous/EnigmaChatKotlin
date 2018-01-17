package alexko.enigmachat

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    // https://code.tutsplus.com/tutorials/how-to-create-an-android-chat-app-using-firebase--cms-27397

    private var adapter: FirebaseListAdapter<ChatMessage>? = null // list adapter of ChatMessage objects
    internal var SIGN_IN_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        if (FirebaseAuth.getInstance().currentUser == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            )
        } else {
            // User is already signed in. Therefore, display
            // a welcome Toast
            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .currentUser!!
                            .displayName!!,
                    Toast.LENGTH_LONG)
                    .show()

            // Load chat room contents
            displayChatMessages()
        }



        // when the floatingactionbutton is clicked, get input, push value of input to firebase as new instance
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val input = findViewById<EditText>(R.id.input)

            // Read the input field and push a new instance
            // of ChatMessage to the Firebase database
            FirebaseDatabase.getInstance()
                    .reference
                    .push() // push method automatically generates a new key
                    .setValue(ChatMessage(input.text.toString(),
                            FirebaseAuth.getInstance()
                                    .currentUser!!
                                    .displayName!!)
                    )
            // Clear the input
            input.setText("")
        }



    }



    private fun displayChatMessages() {
        val listOfMessages = findViewById<ListView>(R.id.list_of_messages)

        adapter = object : FirebaseListAdapter<ChatMessage>(this, ChatMessage::class.java,
                R.layout.message, FirebaseDatabase.getInstance().reference) {
            override fun populateView(v: View, model: ChatMessage, position: Int) {
                // Get references to the views of message.xml
                val messageText = v.findViewById<TextView>(R.id.message_text)
                val messageUser = v.findViewById<TextView>(R.id.message_user)
                val messageTime = v.findViewById<TextView>(R.id.message_time)

                // Set their text
                messageText.text = model.messageText // getMessageText() is a function within ChatMessage.java
                messageUser.text = model.messageUser // getMessageUser() is a function within ChatMessage.java

                // Format the date before showing it
                messageTime.text = DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.messageTime)
            }
        }
        listOfMessages.adapter = adapter
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG)
                        .show()
                displayChatMessages()
            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show()

                // Close the app
                finish()
            }
        }

    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_sign_out) {  // if the id of the item selected is called menu_sign_out
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener {
                        Toast.makeText(this@MainActivity,
                                "You have been signed out.",
                                Toast.LENGTH_LONG)
                                .show()
                        // Close activity
                        finish()
                    }
        }
        return true
    }



} // end main activity class
