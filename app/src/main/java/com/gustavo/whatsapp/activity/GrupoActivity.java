package com.gustavo.whatsapp.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.gustavo.whatsapp.adapter.ContatosAdapter;
import com.gustavo.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.gustavo.whatsapp.config.ConfiguracaoFirebase;
import com.gustavo.whatsapp.databinding.ActivityGrupoBinding;

import com.gustavo.whatsapp.R;
import com.gustavo.whatsapp.helper.RecyclerItemClickListener;
import com.gustavo.whatsapp.helper.UsuarioFirebase;
import com.gustavo.whatsapp.model.Usuario;

import java.io.Serializable;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMembrosSelecionados,recyclerViewMembros;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuariosRef;
    private FloatingActionButton floatingActionButton;
    private Toolbar toolbar;
    private FirebaseUser usuarioAtual;




    private AppBarConfiguration appBarConfiguration;
    private ActivityGrupoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Novo Grupo");


        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        recyclerViewMembros = findViewById(R.id.recyclerMembros);
        recyclerViewMembrosSelecionados = findViewById(R.id.recyclerMembrosSelecionados);
        floatingActionButton = findViewById(R.id.fabGrupo);

        contatosAdapter = new ContatosAdapter(listaMembros,getApplicationContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewMembros.setLayoutManager(layoutManager);
        recyclerViewMembros.setHasFixedSize(true);
        recyclerViewMembros.setAdapter(contatosAdapter);

        recyclerViewMembros.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerViewMembros,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecidonado = listaMembros.get(position);

                        listaMembros.remove(usuarioSelecidonado);
                        contatosAdapter.notifyDataSetChanged();

                        listaMembrosSelecionados.add(usuarioSelecidonado);
                        grupoSelecionadoAdapter.notifyDataSetChanged();
                        atualizarMembrosToolbar();


                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));



        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados,getApplicationContext());
        RecyclerView.LayoutManager layoutManagerHori = new LinearLayoutManager(
          getApplicationContext(),LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerViewMembrosSelecionados.setLayoutManager(layoutManagerHori);
        recyclerViewMembrosSelecionados.setHasFixedSize(true);
        recyclerViewMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        recyclerViewMembrosSelecionados.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(), recyclerViewMembrosSelecionados,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                Usuario usuarioSelecionados  =listaMembrosSelecionados.get(position);
                                listaMembrosSelecionados.remove(usuarioSelecionados);
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                listaMembros.add(usuarioSelecionados);
                                contatosAdapter.notifyDataSetChanged();
                                atualizarMembrosToolbar();

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );





        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GrupoActivity.this, CadastroGrupoActivity.class);
                i.putExtra("membros",(Serializable) listaMembrosSelecionados);
                startActivity(i);
            }
        });
    }
    public void recuperarContatos(){
        valueEventListenerMembros = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for(DataSnapshot dados : snapshot.getChildren()){
                    Usuario usuario =dados.getValue(Usuario.class);

                    String emailUsuarioAtual = usuarioAtual.getEmail();

                    if(!emailUsuarioAtual.equals(usuario.getEmail())){
                        listaMembros.add(usuario);
                    }


                }
                contatosAdapter.notifyDataSetChanged();
                atualizarMembrosToolbar();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void atualizarMembrosToolbar(){
        int totalSelecionado = listaMembrosSelecionados.size();
        int total = listaMembros.size() + totalSelecionado;

        toolbar.setSubtitle(totalSelecionado +" de " + total + " selecionados");
    }


    @Override
    protected void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerMembros);
    }

}