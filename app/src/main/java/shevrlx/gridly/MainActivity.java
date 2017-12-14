package shevrlx.gridly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView displayHighScore = null; // Textview that displays current highscore
    private MainGamePanel gamePanel = null; // the game engine
    private int highScore = 0; // actual highscore number
    private ImageView volume = null; // Image of the volume, mute when pressed

    private SharedPreferences sharedPreferences = null; // preferences

    private static InterstitialAd mInterstitialAd;
    private static AdRequest adRequest2;

    public String PACKAGE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        // ads
        MobileAds.initialize(this, "ca-app-pub-3200394340168372~2273356043");
        // actual adview
        AdView adView = (AdView) findViewById(R.id.adView);
        // gets request for the ad and loads it
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        // load ad
        adView.loadAd(adRequest);
        // initialize view variables
        displayHighScore = (TextView) findViewById(R.id.start_screen_high_score);
        volume = (ImageView) findViewById(R.id.volume);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        highScore = sharedPreferences.getInt(getString(R.string.saved_high_score), 0);

        String displayHighScoreString = getString(R.string.your_high_score) + highScore;
        displayHighScore.setText(displayHighScoreString);

        if (!sharedPreferences.getBoolean("sound", true))
            volume.setImageResource(R.drawable.volumemute);

        setBackgroundColor(R.color.colorPrimary);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // load interstitial ad
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3200394340168372/7927780043");
        adRequest2 = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mInterstitialAd.loadAd(adRequest2);

        PACKAGE_NAME = getApplicationContext().getPackageName();
    }

    private void setBackgroundColor(int color) {
        // set bckgr color
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            displayHighScore.getRootView().setBackgroundColor(getResources().getColor(color, null));
        else
            displayHighScore.getRootView().setBackgroundColor(getResources().getColor(color));
    }

    // called when volume button is clicked
    public void changeVolumeState(View v) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // change audio preference
        if (sharedPreferences.getBoolean("sound", true)) {
            editor.putBoolean("sound", false);
            volume.setImageResource(R.drawable.volumemute);
        } else {
            editor.putBoolean("sound", true);
            volume.setImageResource(R.drawable.volume);
        }
        editor.commit();
    }

    public void openTutorial(View v) {
//        startActivity(new Intent(MainActivity.this, TutorialActivity.class));
    }

    public void startGame(View view) {
        gamePanel = new MainGamePanel(this);
        setContentView(gamePanel);
    }

    public void rateApp(View view) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + PACKAGE_NAME)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + PACKAGE_NAME)));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (gamePanel != null)
            gamePanel.shutDownThread();
        super.onStop();
    }

    public static boolean isLoaded(InterstitialAd interstitialAd) {
        return interstitialAd.isLoaded();
    }

    public static InterstitialAd getmInterstitialAd() {
        return mInterstitialAd;
    }

    public static void refreshAd() {
        mInterstitialAd.loadAd(adRequest2);
    }
}
