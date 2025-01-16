package iuh.fit.se;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 101;

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Replace with your Web Client ID
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI elements
        EditText usernameEditText = findViewById(R.id.username);
        EditText passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);
        Button googleSignInButton = findViewById(R.id.button);
        logoutButton = findViewById(R.id.button2);

        // Set initial visibility for Log out button
        updateUI(mAuth.getCurrentUser());

        // Handle Google Sign-In button
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        // Handle Log Out button
        logoutButton.setOnClickListener(v -> logOut());

        // Handle Login with Email and Password button
        loginButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            } else {
                signInWithEmailAndPassword(email, password);
            }
        });

        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "signInWithCredential:success, user: " + (user != null ? user.getEmail() : "null"));

                        Toast.makeText(this, "Welcome anh Hung dep trai, " + (user != null ? user.getDisplayName() : "User"), Toast.LENGTH_SHORT).show();
                        updateUI(user);
                    } else {
                        // Sign-in failed
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "signInWithEmail:success, user: " + (user != null ? user.getEmail() : "null"));
                        EditText usernameEditText = findViewById(R.id.username);
                        EditText passwordEditText = findViewById(R.id.password);

                        // Đảm bảo rằng chúng không bị null và sau đó xóa nội dung
                        if (usernameEditText != null && passwordEditText != null) {
                            usernameEditText.setText("");
                            passwordEditText.setText("");
                        }
                        Toast.makeText(this, "Welcome back, " + (user != null ? user.getEmail() : "User"), Toast.LENGTH_SHORT).show();
                        updateUI(user);
                    } else {
                        // Sign-in failed
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(this, "Authentication Failed. Please check your email and password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logOut() {
        // Sign out from Firebase
        mAuth.signOut();

        // Sign out from Google
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "User logged out.");
            updateUI(null);
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user == null) {
            // User is logged out
            logoutButton.setVisibility(View.GONE);
        } else {
            // User is logged in
            logoutButton.setVisibility(View.VISIBLE);
        }
    }
}
