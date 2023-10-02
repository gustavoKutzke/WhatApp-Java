package com.gustavo.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.gustavo.whatsapp.R;
import com.gustavo.whatsapp.activity.ChatActivity;
import com.gustavo.whatsapp.adapter.ConversasAdapter;
import com.gustavo.whatsapp.config.ConfiguracaoFirebase;
import com.gustavo.whatsapp.helper.RecyclerItemClickListener;
import com.gustavo.whatsapp.helper.UsuarioFirebase;
import com.gustavo.whatsapp.model.Conversa;
import com.gustavo.whatsapp.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConversaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConversaFragment extends Fragment {


    private RecyclerView recyclerViewConversas;
    private List<Conversa> listaConversas = new ArrayList<>();
    private ConversasAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;



    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public ConversaFragment() {

    }


    public static ConversaFragment newInstance(String param1, String param2) {
        ConversaFragment fragment = new ConversaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_conversa, container, false);

        recyclerViewConversas = view.findViewById(R.id.recyclerListaConversas);

        adapter = new ConversasAdapter(listaConversas,getActivity());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversas.setLayoutManager(layoutManager);
        recyclerViewConversas.setHasFixedSize(true);
        recyclerViewConversas.setAdapter(adapter);

        recyclerViewConversas.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                recyclerViewConversas,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Conversa conversaSelecionada = listaConversas.get(position);

                        if(conversaSelecionada.getIsGroup().equals("true")){
                            Intent i = new Intent(getActivity(), ChatActivity.class);
                            i.putExtra("chatGrupo",conversaSelecionada.getGrupo());
                            startActivity(i);

                        }else {
                            Intent i = new Intent(getActivity(), ChatActivity.class);
                            i.putExtra("chatContato",conversaSelecionada.getUsuario());
                            startActivity(i);
                        }



                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }


        ));


        String identifadorUsuario = UsuarioFirebase.getIdentificadorUsuario();

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        conversasRef= database.child("conversas").child(identifadorUsuario);


        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener(childEventListenerConversas);
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

//    public void pesquisarConversas(String texto){
//
//        List<Conversa> listaConversaBusca = new ArrayList<>();
//        for(Conversa conversa : listaConversas){
//            String nome = conversa.getUsuario().getNome().toLowerCase();
//            String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();
//
//            if(nome.contains(texto) || ultimaMsg.contains(texto)){
//                listaConversaBusca.add(conversa);
//            }
//        }
//
//        adapter = new ConversasAdapter(listaConversaBusca,getActivity());
//        recyclerViewConversas.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
//
//    }

    public void recuperarConversas(){

        listaConversas.clear();


        childEventListenerConversas =conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Conversa conversa = snapshot.getValue(Conversa.class);
                listaConversas.add(conversa);
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