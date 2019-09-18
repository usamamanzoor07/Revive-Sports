package view

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.api.GoogleApiClient
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class PhoneHintActivity: FragmentActivity(), GoogleApiClient.ConnectionCallbacks {
    override fun onConnected(p0: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //phone Authentication process
    var apiClient: GoogleApiClient? = null
    val RESOLVE_HINT = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getCredentialApiClient()
        requestHint()

    }


    private fun getCredentialApiClient() {
        apiClient = GoogleApiClient.Builder(baseContext)
            .addConnectionCallbacks(this)
            .addApi(Auth.CREDENTIALS_API)
            .build()
            }


    // Construct a request for phone numbers and show the picker
    private fun requestHint() {
        val hintRequest: HintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val intent: PendingIntent = Auth.CredentialsApi.getHintPickerIntent(
            apiClient, hintRequest
        )
        startIntentSenderForResult(intent.intentSender, RESOLVE_HINT, null, 0, 0, 0)
            }




    // Obtain the phone number from the result
    override fun onActivityResult(requestCode:Int,  resultCode:Int,data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                val credential: Credential = data!!.getParcelableExtra(Credential.EXTRA_KEY)
                // credential.getId(); <-- E.164 format phone number on 10.2.+ devices
                if(credential != null)
                {
                   val number=credential.id
                    val intent = Intent()
                    intent.putExtra("number",number)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                else{longToast("Number not selected")}
            }
        }
        finish()
    }








}