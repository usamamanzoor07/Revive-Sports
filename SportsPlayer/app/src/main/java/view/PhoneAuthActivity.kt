package view

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sportsplayer.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.pawegio.kandroid.visible
import kotlinx.android.synthetic.main.activity_phone_auth.*
import kotlinx.android.synthetic.main.player_signup.*
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class PhoneAuthActivity : AppCompatActivity(), View.OnClickListener {
    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]
    var progressdialog:ProgressDialog?=null
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)
        progressdialog= ProgressDialog(this)
        //get values sent from SignUp or SignIn activity
        getIntentValue()
        // Assign click listeners
        verifyPhone_Button_PhoneAuthActivity.setOnClickListener(this)
        sendCode_Button_PhoneAuthActivity.setOnClickListener(this)
        auth = FirebaseAuth.getInstance()
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.d(TAG, "onVerificationCompleted:$credential")
                updateUI(credential)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                verifyPhone_Button_PhoneAuthActivity.loadingFailed()
                toast("Please enter the correct phone number with country code")
                makeViewsVisible(sendCode_Button_PhoneAuthActivity,phoneNumber_textView_PhoneAuthActivity)
                makeViewsInVisible(verifyPhone_Button_PhoneAuthActivity,code_textView_PhoneAuthActivity)
                Log.w(TAG, "onVerificationFailed", e)
                if (e is FirebaseAuthInvalidCredentialsException) {
                    phoneNumber_textView_PhoneAuthActivity.error = "Invalid phone number."
                } else if (e is FirebaseTooManyRequestsException) {
                        toast("Quota exceeded.")
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")
                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
                progressdialog?.dismiss()
                makeViewsInVisible(sendCode_Button_PhoneAuthActivity,phoneNumber_textView_PhoneAuthActivity)
                makeViewsVisible(verifyPhone_Button_PhoneAuthActivity,code_textView_PhoneAuthActivity)
            }
        }
        // [END phone_auth_callbacks]
    }

    private fun updateUI(cred: PhoneAuthCredential) {
        // Set the verification text based on the credential
        if (cred.smsCode != null) {
            code_textView_PhoneAuthActivity.setText(cred.smsCode)
        } else {
            code_textView_PhoneAuthActivity.setText(R.string.instant_validation)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && data!=null)
        {
            when(requestCode){
                PHONE_HINT->{
                    val number = data.getStringExtra("number")
                    phoneNumber_textView_PhoneAuthActivity.setText(number)
                }
            }
        }else{toast("something went wrong number Not Received")}
    }

    private fun getIntentValue()
    {
        val number=intent.getStringExtra("number")
        val signIn=intent.getStringExtra("signIn")
        val signUp=intent.getStringExtra("signUp")
        when {
            signUp=="7" -> phoneNumber_textView_PhoneAuthActivity.setText(number)
            signIn=="8" -> startActivityForResult<PhoneHintActivity>(PHONE_HINT)
            else -> toast("No Intent found")
        }
    }

    override fun onResume() {
        super.onResume()
       makeViewsInVisible(verifyPhone_Button_PhoneAuthActivity,code_textView_PhoneAuthActivity)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        Log.d(TAG,"Start number verification")
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks) // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
    }

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    toast("Successfully SignIn...")
                    progressdialog?.dismiss()
                    // [SEND RESULT TO PLAYER REGISTRATION]
                    val number=phoneNumber_textView_PhoneAuthActivity.text.toString().trim()
                    val intent = Intent()
                    intent.putExtra("status","1")
                    intent.putExtra("number",number)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                    // [END SENDING RESULT]

                } else {
                    progressdialog?.dismiss()
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        toast("Invalid Code ....SignIn failed")
                        code_textView_PhoneAuthActivity.error = "Invalid code."
                        finish()
                        // [END_EXCLUDE]
                    }
                }
            }
    }
    // [END sign_in_with_phone]
    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = phoneNumber_textView_PhoneAuthActivity.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumber_textView_PhoneAuthActivity.error = "Invalid phone number."
            return false
        }
        return true
    }

    private fun verifyPhoneWithCodeSent()
    {
        val code = code_textView_PhoneAuthActivity.text.toString()
        if (TextUtils.isEmpty(code)) {
            code_textView_PhoneAuthActivity.error = "Cannot be empty."
            return
        }else
        {
            progressdialog?.setTitle("Code Verification")
            progressdialog?.setMessage("please wait,we are verifying your code...")
            progressdialog?.setCanceledOnTouchOutside(false)
            progressdialog?.show()
            Log.d(TAG,"verify Phone number with code")
            // [START verify_with_code]
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
            // [END verify_with_code]
            signInWithPhoneAuthCredential(credential)
        }
    }
    fun makeViewsVisible(vararg view:View)
    { for(v in view) { v.visible=true } }

    fun makeViewsInVisible(vararg view:View)
    { for(v in view) { v.visible=false } }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.sendCode_Button_PhoneAuthActivity->{
                if(validatePhoneNumber())
                {   progressdialog?.setTitle("Phone Verification")
                    progressdialog?.setMessage("please wait,while we are authenticating your number...")
                    progressdialog?.setCanceledOnTouchOutside(false)
                    progressdialog?.show()
                    val phoneNumber=phoneNumber_textView_PhoneAuthActivity.text.toString().trim()
                    makeViewsInVisible(phoneNumber_textView_PhoneAuthActivity,sendCode_Button_PhoneAuthActivity)
                    makeViewsVisible    (verifyPhone_Button_PhoneAuthActivity,code_textView_PhoneAuthActivity)
                    startPhoneNumberVerification(phoneNumber)
                }else { toast("Invalid Phone Number") }
            }
            R.id.verifyPhone_Button_PhoneAuthActivity -> {
                makeViewsInVisible(sendCode_Button_PhoneAuthActivity,phoneNumber_textView_PhoneAuthActivity)
                verifyPhoneWithCodeSent()
            }
        }
    }
    companion object {
        private const val TAG = "PhoneAuthActivity"
        const val PHONE_HINT=7
    }
}