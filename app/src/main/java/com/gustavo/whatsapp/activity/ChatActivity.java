package com.gustavo.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gustavo.whatsapp.adapter.MensagensAdapter;
import com.gustavo.whatsapp.config.ConfiguracaoFirebase;
import com.gustavo.whatsapp.databinding.ActivityChatBinding;

import com.gustavo.whatsapp.R;
import com.gustavo.whatsapp.helper.Base64Custom;
import com.gustavo.whatsapp.helper.UsuarioFirebase;
import com.gustavo.whatsapp.model.Conversa;
import com.gustavo.whatsapp.model.Grupo;
import com.gustavo.whatsapp.model.Mensagem;
import com.gustavo.whatsapp.model.Usuario;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityChatBinding binding;
    private TextView textviewNome;
    private CircleImageView circleImageViewFoto;
    private ChildEventListener childEventListenerMensagens;
    private static final int SELECAO_CAMERA = 100;
    private EditText editMensagem;
    private Usuario usuarioRemetente;
    private DatabaseReference database;
    private DatabaseReference mensagensRef;
    private StorageReference storage;
    private String idUsuarioRemetente;
    private List<Mensagem> mensagens = new ArrayList<>();
    private RecyclerView recyclerViewMensagens;
    private ImageView imageCamera;
    private MensagensAdapter adapter;
    private String idUsuarioDestinatario;
    private Grupo grupo;

    private Usuario usuarioDestinatario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        textviewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerViewMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.imageCameraChat);


        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();





        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            if(bundle.containsKey("chatGrupo")){

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                textviewNome.setText(grupo.getNome());

                String foto = grupo.getFoto();
                if(foto != null){
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this).load(url).into(circleImageViewFoto);
                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }


            }else{
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textviewNome.setText(usuarioDestinatario.getNome());

                String foto = usuarioDestinatario.getFoto();
                if(foto != null){
                    Uri url = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this).load(url).into(circleImageViewFoto);
                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }
                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
            }
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewMensagens.setLayoutManager(layoutManager);
        recyclerViewMensagens.setHasFixedSize(true);

        adapter = new MensagensAdapter(mensagens,getApplicationContext());

        recyclerViewMensagens.setAdapter(adapter);

        database =ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente).child(idUsuarioDestinatario);


        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getPackageManager())!=null){
                    startActivityForResult(i,SELECAO_CAMERA);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK) {
            Bitmap imagem = null;

            try {
                switch (requestCode) {
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                }
                if(imagem != null){
                    //Recuperando dados da imagem para o Firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG,70,baos);
                    byte[] dadosImagem = baos.toByteArray();

                    String nomeDaImagem = UUID.randomUUID().toString();

                    final StorageReference imageRef = storage.child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeDaImagem);

                    UploadTask uploadTask = imageRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Erro","Erro ao fazer upload");
                            Toast.makeText(ChatActivity.this,"Erro ao fazer upload da imagem",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                     String dowloadUrl = task.getResult().toString();
                                    if(usuarioDestinatario !=null){

                                        Mensagem mensagem = new Mensagem();
                                        mensagem.setIdUsuario(idUsuarioRemetente);
                                        mensagem.setMensagem("imagem.jpeg");
                                        mensagem.setImagem(dowloadUrl);

                                        salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagem);
                                        salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagem);
                                    }else {
                                        for(Usuario membro:grupo.getMembros()){
                                            String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                                            String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                                            mensagem.setMensagem("imagem.jpeg");
                                            mensagem.setNome(usuarioRemetente.getNome());
                                            mensagem.setImagem(dowloadUrl);

                                            salvarMensagem(idRemetenteGrupo,idUsuarioDestinatario,mensagem);

                                            salvarConversa(idRemetenteGrupo,idUsuarioDestinatario,usuarioDestinatario,mensagem,true);

                                        }
                                    }

                                        Toast.makeText(ChatActivity.this,"Sucesso ao enviar Imagem",Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public void enviarMensagem(View view){



        String textoMensagem = editMensagem.getText().toString();
        if(!textoMensagem.isEmpty()){

            if(usuarioDestinatario !=null){
                Mensagem mensagem = new Mensagem();
                mensagem.setMensagem(textoMensagem);
                mensagem.setIdUsuario(idUsuarioRemetente);

                salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagem);

                salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagem);

                salvarConversa(idUsuarioRemetente,idUsuarioDestinatario,usuarioDestinatario,mensagem,false);


                salvarConversa(idUsuarioDestinatario,idUsuarioRemetente,usuarioRemetente,mensagem,false);

            }else {

                for(Usuario membro:grupo.getMembros()){
                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                    mensagem.setMensagem(textoMensagem);
                    mensagem.setNome(usuarioRemetente.getNome());

                    salvarMensagem(idRemetenteGrupo,idUsuarioDestinatario,mensagem);

                    salvarConversa(idRemetenteGrupo,idUsuarioDestinatario,usuarioDestinatario,mensagem,true);

                }

            }



        }else{
            Toast.makeText(this, "Digite uma mensagem para enviar!", Toast.LENGTH_LONG).show();
        }
    }

    private void salvarConversa(String idRemetente,String idDestinatario,Usuario usuarioExibicao,Mensagem msg,boolean isGroup){

        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());

        if(isGroup){
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);
        }else{
            conversaRemetente.setUsuario(usuarioExibicao);
            conversaRemetente.setIsGroup("false");
        }
        conversaRemetente.salvarConversa();


    }

    private void salvarMensagem(String idDestinatario,String idRemetente,Mensagem mensagem){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef =database.child("mensagens");

        mensagemRef.child(idRemetente).child(idDestinatario).push().setValue(mensagem);
        editMensagem.setText("");

    }
    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }
    private void recuperarMensagens(){

        mensagens.clear();

        childEventListenerMensagens =mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem =snapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}