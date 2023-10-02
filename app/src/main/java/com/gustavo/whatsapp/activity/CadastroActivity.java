package com.gustavo.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.gustavo.whatsapp.R;
import com.gustavo.whatsapp.config.ConfiguracaoFirebase;
import com.gustavo.whatsapp.helper.Base64Custom;
import com.gustavo.whatsapp.helper.UsuarioFirebase;
import com.gustavo.whatsapp.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText campoNome,campoSenha,campoEmail;
    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoEmail = findViewById(R.id.editLoginEmail);
        campoNome = findViewById(R.id.editNome);
        campoSenha = findViewById(R.id.editSenha);
    }
    public void cadastroUsuario(Usuario usuario){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),usuario.getSenha()

        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                 if(task.isSuccessful()){

                    Toast.makeText(CadastroActivity.this,"Sucesso ao cadastrar usuário!!",Toast.LENGTH_SHORT).show();
                     UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                    finish();

                    try {

                        String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setUid(identificadorUsuario);
                        usuario.salvar();


                    }catch (Exception e ){
                        e.printStackTrace();
                    }

                 }else{

                     String excecao = "";
                     try {
                         throw task.getException();

                     }catch (FirebaseAuthWeakPasswordException e ){
                         excecao = "Digite uma senha mais forte";
                     }catch (FirebaseAuthInvalidCredentialsException e ){
                         excecao = "Por favor , digite um e-mail válido";

                     }catch (FirebaseAuthUserCollisionException e ){
                         excecao = "Essa conta já foi cadastrada";

                     }catch (Exception e ){
                         excecao = "Erro ao cadastrar usuário : "+ e.getMessage();
                         e.printStackTrace();
                     }
                     Toast.makeText(CadastroActivity.this,excecao,Toast.LENGTH_LONG).show();




                 }



            }
        });

    }

    public void validarCadastroUsuario(View view){

        String textoNome = campoNome.getText().toString();
        String textoSenha = campoSenha.getText().toString();
        String textoEmail = campoEmail.getText().toString();

        if(!textoNome.isEmpty()){
            if(!textoEmail.isEmpty()){
                if(!textoSenha.isEmpty()){

                    Usuario usuario = new Usuario();
                    usuario.setEmail(textoEmail);
                    usuario.setNome(textoNome);
                    usuario.setSenha(textoSenha);
                    cadastroUsuario(usuario);

                }else {
                    Toast.makeText(CadastroActivity.this,"Preencha a Senha !",Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(CadastroActivity.this,"Preencha o Email !",Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(CadastroActivity.this,"Preencha o nome !",Toast.LENGTH_LONG).show();
        }


    }
}