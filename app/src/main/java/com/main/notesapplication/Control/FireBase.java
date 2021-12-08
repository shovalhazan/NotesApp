package com.main.notesapplication.Control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.main.notesapplication.Model.Note;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FireBase {
    private static FireBase instance;
    private FirebaseStorage storage ;
    private StorageReference storageRef;
    private StorageReference mainRef;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore ;
    private CollectionReference notesCollection;

    private FireBase() {
         storage = FirebaseStorage.getInstance();
         storageRef = storage.getReference();
         firebaseAuth =FirebaseAuth.getInstance();;
         firestore = FirebaseFirestore.getInstance();
         mainRef = storageRef.child("notes_storage");
         notesCollection = firestore.collection("notes");
    }

    public static void init(Context context) {
        //singleton design pattern
        if (instance == null && context == context.getApplicationContext()) {
            instance = new FireBase();
        }
    }

    public void deleteFromFireStore(String noteNumber) {
        notesCollection.document(noteNumber).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("TODO", "onSuccess: ");
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TODO", "onFailure: ");
            }
        });

    }

    public void loginByEmailAndPassword(String email, String password,Context context,OnSuccsessCallBack callBack){
        Log.d("pttt", "loginByEmailAndPassword: ");
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("pttt", "signInWith:success");
                            callBack.isSuccess(true);
                        } else {
                            Log.d("pttt", "signInWith:failure", task.getException());
                            callBack.isSuccess(false);
                        }
                    }
                });
    }

    public void addUserByEmailAndPassword(String email, String password, Context context){
        Log.d("pttt", "addUserByEmailAndPassword: ");
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("pttt", "createUserWithEmail:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("pttt", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
    }
    public boolean checkCurrentUser(){
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null)
            return  true;
        else
            return false;
    }

    public FirebaseUser getCurrentUser(){
        return firebaseAuth.getCurrentUser();
    }
    public void signOut (){
        FirebaseAuth.getInstance().signOut();;

    }
    private Map<String, Object> makeNoteMap(Note newNote){
        Map<String, Object> note = new HashMap<>();
        note.put("title",newNote.getTitle());
        note.put("body",newNote.getBody());
        note.put("imageId",newNote.getImageId());
        note.put("date",newNote.getDate());
        note.put("lat",newNote.getLat());
        note.put("lon",newNote.getLon());
        return note;
    }
    public void addNoteInFireStore(Note note){
        Map<String, Object> n = makeNoteMap(note);
        // Add a new document with a generated ID
        notesCollection.add(n)
              .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                  @Override
                  public void onSuccess(DocumentReference documentReference) {
                      documentReference.update("noteNumber",documentReference.getId());
                  }
              })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("pttt", "Error adding document", e);
                    }
                });
    }
    public void updateNoteInFireStore(Note note) {
        Log.d("pttt", "updateNoteInFireStore: "+note.getNoteNumber());
        Map<String, Object> n = makeNoteMap(note);
        notesCollection.document(note.getNoteNumber()).update(n)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("pttt", "Error adding document", e);
                    }
                });
    }
    public static FireBase getInstance() {
        return instance;
    }


    public void uploadImageToCloud(Bitmap bitmap, OnImageSaveListener listener) {
        Log.d("pttt", "uplodeImage: ");
        if(bitmap!=null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageInByte = stream.toByteArray();

            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
            mainRef = storageRef.child("notes_storage");
            StorageReference imageRef = mainRef.child(UUID.randomUUID().toString() + ".png");

            UploadTask uploadTask = imageRef.putBytes(imageInByte);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d("pttt", "onFailure: ");
                    // Handle unsuccessful uploads
                    listener.imageSaved(null);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d("Pttt", "UPLODE onSuccess: ");
                    listener.imageSaved(imageRef.getPath());
                    Log.d("pttt", "onSuccess: "+imageRef.getPath());
                }
            });

        }else
            listener.imageSaved(null);

    }

    public CollectionReference getNotesCollection() {
        return notesCollection;
    }

    public void getListItems(GetListListener listListener) {
        notesCollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                            List<Note> types = documentSnapshots.toObjects(Note.class);
                            listListener.getList(types);
                            Log.d("pttt", "onSuccess: " );
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TODO", "onFailure: ");
                            listListener.getList(null);
                        }
                });
    }
    public void downloadStorageData(String imageId, Context context, ImageView imageView) {
        Log.d("pttt", "downloadStorageData: ImageId  :" + imageId);
        StorageReference noteRef = storage.getReferenceFromUrl("https://firebasestorage.googleapis.com/v0/b/notesapp-e49b4.appspot.com/o" + imageId);
        if (noteRef != null) {
            noteRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d("pttt", "onSuccess: " + uri);
                    // create a ProgressDrawable object which we will show as placeholder
                    CircularProgressDrawable drawable = new CircularProgressDrawable(context);
                    drawable.setCenterRadius(30f);
                    drawable.setStrokeWidth(5f);
                    // set all other properties as you would see fit and start it
                    drawable.start();
                    Glide.with(context).load(uri).placeholder(drawable).into(imageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
        }
    }
}
