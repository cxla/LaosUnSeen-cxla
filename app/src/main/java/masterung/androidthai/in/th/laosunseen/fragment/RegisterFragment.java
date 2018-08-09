package masterung.androidthai.in.th.laosunseen.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import masterung.androidthai.in.th.laosunseen.MainActivity;
import masterung.androidthai.in.th.laosunseen.R;
import masterung.androidthai.in.th.laosunseen.utility.MyAlert;
import masterung.androidthai.in.th.laosunseen.utility.UserModel;

public class RegisterFragment extends Fragment {

    //    Explicit
    private Uri uri;
    private ImageView imageView;
    private Boolean aBoolean = true;
    private String nameString, emailString,passwordString,
            uidString,pathURLStirng,myPostString;
    private ProgressDialog progressDialog;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        Create Toolbar
        createToolbar();

//        Photo Controller
        photoController();

    }   // Main Class

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_register, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.itemUpload) {
            uploadProcess();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void uploadProcess() {
        //ສະແດງໄມ້ກັນລະຫວ່າງໂປຣແກັມເຮັດວຽກ
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Upload Value process");
        progressDialog.setMessage("Please waite ຈັກນ່ອຍ");
        progressDialog.show();

        EditText nameEditText = getView().findViewById(R.id.edtName);
        EditText emailEditText = getView().findViewById(R.id.edtEmail);
        EditText passwordEditText = getView().findViewById(R.id.edtPassword);

//        Get Value From EditText
        nameString = nameEditText.getText().toString().trim();
        emailString = emailEditText.getText().toString().trim();
        passwordString = passwordEditText.getText().toString().trim();



//        check choose pthoto
        if (aBoolean) {
//          non choose Photo
            MyAlert myAlert = new MyAlert(getActivity());//getActivity() ການເກັດ ຄອນແທັກ
            myAlert.nornalDialog("Non Choose Photo", "Please Choose Photo");
        } else if (nameString.isEmpty() || emailString.isEmpty() || passwordString.isEmpty()) {
//            Have Space
            MyAlert myAlert = new MyAlert(getActivity());
            myAlert.nornalDialog("Have Space", "Please Fill All Every Blank");

        } else {
//            No Space

            createAuthentication();

            UploadPhotoToFirebase();//Alt+Enter+Enter ສ້າງເມຕອດເອງ
        }
    }

    private void createAuthentication() {
        Log.d("8AugV1","CreateAuthen work");// ວິທີດີບັກ ໃຫ້ມັນສະແດງຜົນອອກທາງຈໍ Logcat ALT+6

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(emailString, passwordString)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("8AugV1","Successful");// ວິທີດີບັກ ໃຫ້ມັນສະແດງຜົນອອກທາງຈໍ Logcat ALT+6
                            uidString = firebaseAuth.getCurrentUser().getUid();
                            Log.d("8AugV1","uidStirng==>"+uidString);

                        } else {

                            MyAlert myAlert = new MyAlert(getActivity());
                            myAlert.nornalDialog("Cannot register","Because ==>"+task.getException().getMessage());//ເອົາຂໍ້ຄວາມທີ່ບໍ່ຜ່ານອອກມາສະແດງ
                            Log.d("8AugV1", "Error ==>" + task.getException().getMessage());
                            progressDialog.dismiss();
                        }
                    }
                });

    }

    private void UploadPhotoToFirebase() {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();
        StorageReference storageReference1 = storageReference.child("Avata/"+nameString);


        storageReference1.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(), "Success Upload Photo", Toast.LENGTH_SHORT).show();
                findPathURLPhoto();
                createPost();
                createDatabase();
                progressDialog.dismiss();;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Cannot Upload Photo", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("9AugV1", "e CreateDatabase ==>" + e.toString());
                progressDialog.dismiss();
            }
        });

    }//Upload Photo

    private void createDatabase() {
        //1.ດຶງໂມເດວ ມາກ່ອນ
        UserModel userModel = new UserModel(uidString, nameString, emailString, pathURLStirng, myPostString);
        //2.
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference()
                .child("User");
        databaseReference.child(uidString).setValue(userModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(),"Register Success",Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.contentMainFragment, new ServiceFragment())
                                .commit();
                    }
                });

    }//createDatabase

    private void createPost() {
        ArrayList<String> stringsArrayList = new ArrayList<>();
        stringsArrayList.add("Hello");
        myPostString = stringsArrayList.toString();
        Log.d("9AugV1", "myPost ==> " + myPostString);

    }

    private void findPathURLPhoto() {
        try { // Code ທີ່ສ່ຽງຕໍ່ການ Error ໃຫ້ໃສ່  Try Catch

            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference();
            // ດຶງມາໃນຮູບແບບຂອງສະຕິງອາເລ
            final String[] urlStrings = new String[1];
            storageReference.child("Avata").child(nameString)
            .getDownloadUrl()
            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    urlStrings[0] = uri.toString();
                    pathURLStirng = urlStrings[0];
                    Log.d("9AugV1", "pathURL ==>" + pathURLStirng);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("9AugV1", "e Error ==>" + e.toString());
                }
            });


        } catch (Exception e) {
            e.printStackTrace();

        }
    }//FindPath

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {


            uri = data.getData();
            aBoolean = false;
            try {

                Bitmap bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri));
                Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 400, 300, true);
                imageView.setImageBitmap(bitmap1);

            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            Toast.makeText(getActivity(), "Please Choose Photo", Toast.LENGTH_SHORT).show();
        }

    }

    private void photoController() {
        imageView = getView().findViewById(R.id.imvPhoto);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Please Choose App"), 1);

            }
        });
    }

    private void createToolbar() {
        Toolbar toolbar = getView().findViewById(R.id.toolbarRegister);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Register");
        ((MainActivity) getActivity()).getSupportActionBar().setSubtitle("Please Choose Photo and Fill All Blank");
        ((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        return view;
    }
}
