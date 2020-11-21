//package com.example.myapplication;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//import android.view.MotionEvent;
//import android.widget.Toast;
//
//import com.google.ar.core.Anchor;
//import com.google.ar.core.HitResult;
//import com.google.ar.core.Plane;
//import com.google.ar.sceneform.AnchorNode;
//import com.google.ar.sceneform.rendering.Material;
//import com.google.ar.sceneform.rendering.ModelRenderable;
//import com.google.ar.sceneform.ux.ArFragment;
//import com.google.ar.sceneform.ux.BaseArFragment;
//import com.google.ar.sceneform.ux.TransformableNode;
//
//public class MainActivity extends AppCompatActivity {
//    private ArFragment arFragment;
//    private ModelRenderable modelRenderable;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
//        setUpModel();
//        setUpPlane();
//    }
//    private void setUpModel(){
//        ModelRenderable.builder()
//                .setSource(this,R.raw.hamburger)
//                .build()
//                .thenAccept(renderable->modelRenderable = renderable)
//                .exceptionally(throwable -> {
//                    Toast.makeText(MainActivity.this,"not loaded",Toast.LENGTH_LONG).show();
//                    return(null);
//                });
//
//    }
//    private void setUpPlane(){
//        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
//            @Override
//            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
//                Anchor anchor = hitResult.createAnchor();
//                AnchorNode anchorNode = new AnchorNode(anchor);
//                anchorNode.setParent(arFragment.getArSceneView().getScene());
//                createModel(anchorNode);
//            }
//        });
//    }
//    private void createModel(AnchorNode anchorNode){
//        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
//        node.setParent(anchorNode);
//        node.setRenderable(modelRenderable);
//        node.select();
//    }
//}
package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference modelRef = storage.getReference().child("restaurant/dish/out.glb");

        ArFragment arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.arFragment);

        findViewById(R.id.downloadBtn)
                .setOnClickListener(v -> {

                    try {
                        File file = File.createTempFile("out", "glb");

                        modelRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                buildModel(file);

                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
            anchorNode.setRenderable(renderable);
            Vector3 vector3 = new Vector3(0.1f,0.1f,0.1f);
            anchorNode.setLocalScale(vector3);
            arFragment.getArSceneView().getScene().addChild(anchorNode);


        });
    }

    private ModelRenderable renderable;

    private void buildModel(File file) {

        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(file.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable
                .builder()
                .setSource(this, renderableSource)
                .setRegistryId(file.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    Toast.makeText(this, "Model built", Toast.LENGTH_SHORT).show();;
                    renderable = modelRenderable;

                });

    }
}