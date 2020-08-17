package uk.co.verifymyage.sdk

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

public final class VerifyMyAgeVerificationFlow : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verifying_age_screen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#f9ac19")))
        supportActionBar?.title = Html.fromHtml("<font color='#ffffff'>VerifyMyAge</font>")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#fbc359")
        };

        doAsync {
            var vmaApi = VMAApi(
                intent.getStringExtra("API_ID").toString(),
                intent.getStringExtra("API_KEY").toString(),
                intent.getStringExtra("API_SECRET").toString()
            );
            var customer  = VmaCustomer(
                intent.getStringExtra("id").toString(),
                intent.getStringExtra("firstName").toString(),
                intent.getStringExtra("lastName").toString(),
                intent.getStringExtra("email").toString(),
                intent.getStringExtra("phone").toString()
            );
            var response = vmaApi.verifications(customer)

            //TODO:
            // 1. it is here just for tests, remove it from here and use it after face recognized
            // 2. Replace R.drawable.face_test with the face detected image
            val bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.face_test)
            vmaApi.faceUpdate(response.getString("hash"), bitmap)

            val intent = Intent(this, VerificationsResultActivity::class.java).apply {
                putExtra("REAUTHENTICATE", response.getBoolean("reauthenticate"))
                putExtra("CLIENT_ID", response.getString("client_id"))
                putExtra("STATUS", response.getString("status"))
                putExtra("URL", response.getString("url"))
            }
            startActivity(intent)
        }.execute()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }
    }

}