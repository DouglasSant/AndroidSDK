package uk.co.verifymyage.sdk

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.verification_results_screen.*
import uk.co.verifymyage.sdk.zoomprocessors.LivenessCheckProcessor

class VerificationsResultActivity : AppCompatActivity() {

    private var isToReautheticate = false;
    private var sessionTokenErrorCallback = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verification_results_screen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#f9ac19")))
        supportActionBar?.title = Html.fromHtml("<font color='#ffffff'>VerifyMyAge</font>")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#fbc359")
        };

        isToReautheticate = intent.getBooleanExtra("REAUTHENTICATE", false);
        if (!isToReautheticate) {
            successText.text = "Success";
            titleText.text = "Select continue to open camera to take a photo"
            subTitleText.text = "Your photo will be used to confirm you've verified your age for future shopping."
        }
    }

    fun onContinueClick(v: View) {
        if (isToReautheticate) {
            val intent = Intent(this, VerifyOptionsActivity::class.java).apply {
                putExtra("VERIFICATION_RESULT", true)
            }
            startActivity(intent)
            return;
        }

        //TODO: fix sessionTokenErrorCallback
        var latestProcessor = LivenessCheckProcessor(this, null)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}