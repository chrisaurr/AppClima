package com.example.appclima;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConsultWeather#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConsultWeather extends Fragment {
    //Variables de inicialización
    EditText pais, ciudad;
    Button obtenerInfo;
    Boolean ingreso = false;
    String longitude, latitude, country, city, nomPais, nomCiudad;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ConsultWeather() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConsultWeather.
     */
    // TODO: Rename and change types and number of parameters
    public static ConsultWeather newInstance(String param1, String param2) {
        ConsultWeather fragment = new ConsultWeather();
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_consult_weather, container, false);

        //Asignamos a cada variale con su respectivo elemento en la vista
        pais = view.findViewById(R.id.txtPais);
        ciudad = view.findViewById(R.id.txtCiudad);
        obtenerInfo = view.findViewById(R.id.btnClima);


        if(savedInstanceState != null){
            nomPais = savedInstanceState.getString("ResultadoP");
            nomCiudad = savedInstanceState.getString("ResultadoC");
            obtenerInfoUbicacion(nomPais, nomCiudad, pais, ciudad);
        }/*else{
            pais.setText("Guatemala");
            ciudad.setText("El Progreso");
            obtenerInfoUbicacion("Guatemala", "El Progreso", pais, ciudad);
        }*/

        obtenerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtenemos el nombre del pais y la ciudad
                nomPais = pais.getText().toString();
                nomCiudad = ciudad.getText().toString();

                //En base a eso ejecutamos una función para obtener la longitud y latitud
                obtenerInfoUbicacion(pais.getText().toString(), ciudad.getText().toString(), pais, ciudad);

            }
        });

        return view;
    }

    private void obtenerInfoUbicacion(String pais, String ciudad, EditText v, EditText v1){
        OkHttpClient client = new OkHttpClient();

        //Api para obtener geolocalización
        String url = "https://api.api-ninjas.com/v1/geocoding?city="+ciudad+"&country="+pais+"";

        //Creamos un objeto para la consulta
        okhttp3.Request request = new okhttp3.Request.Builder().url(url)
                .header("X-Api-Key", "wVYx0yBgMbKHV8o7a+qkSg==Q3wwz8dsTKWwMEZz")
                .build();

        client.newCall(request).enqueue(new Callback() {
            //En caso de fallo
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if(response.isSuccessful()){ //Si la respuesta es satisfactoria
                    String myResponse = response.body().string();

                    //Ejecutamos un hilo para hacer la petición asincrona a la ejecución del programa
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //Deserealizamos y guardamos lo obtenido de la api en variables
                                JSONArray jsonObject = new JSONArray(myResponse);
                                JSONObject json = jsonObject.getJSONObject(0);
                                longitude = json.getString("longitude");
                                latitude = json.getString("latitude");
                                country = v.getText().toString();
                                city = v1.getText().toString();

                                //La añadimos a un Bundler para pasar todos los elementos en un solo parametro
                                Bundle bundle = new Bundle();
                                bundle.putString("longitud", longitude.toString().trim());
                                bundle.putString("latitud", latitude.toString().trim());
                                bundle.putString("pais", country.toString().trim());
                                bundle.putString("ciudad", city.toString().trim());

                                getParentFragmentManager().setFragmentResult("datos", bundle);

                                Log.v("Prueba", longitude);
                                Log.v("Prueba", latitude);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }
        });


    }

    //En el caso de rotar la pantalla recuperamos los datos para mostrar la ultima consulta que se hizo
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("ResultadoP", nomPais);
        outState.putString("ResultadoC", nomCiudad);
    }
}