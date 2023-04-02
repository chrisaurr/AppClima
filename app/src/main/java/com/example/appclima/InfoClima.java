package com.example.appclima;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InfoClima#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoClima extends Fragment {

    //Variables de inicialización
    TextView view, clima, clima_max, clima_min;
    ImageView img;
    String longitude, latitude, pais, ciudad;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public InfoClima() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InfoClima.
     */
    // TODO: Rename and change types and number of parameters
    public static InfoClima newInstance(String param1, String param2) {
        InfoClima fragment = new InfoClima();
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

        getParentFragmentManager().setFragmentResultListener("datos", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                //Valores obtenidos desde el fragment ConsultWeather cada vez que se presione su botón
                longitude = result.getString("longitud");
                latitude = result.getString("latitud");
                pais = result.getString("pais");
                ciudad = result.getString("ciudad");

                //Concatenamos el nombre de la ciudad y el pais en un textview
                view.setText("Clima en "+ciudad+", "+pais);

                //Función que hace una llamada a la api openWeather por medio de longitud y latitud
                obtenerInfoClima(longitude, latitude);
            }
        });

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_info_clima, container, false);

        //Asociamos cada variable con un elemento de la vista
        view =  vista.findViewById(R.id.country);
        clima =  vista.findViewById(R.id.clima);
        clima_max =  vista.findViewById(R.id.clima_max);
        clima_min =  vista.findViewById(R.id.clima_min);
        img = vista.findViewById(R.id.imgWeather);

        return vista;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
    }

    private void obtenerInfoClima(String longitud, String latitud){
        //Agregamos tiempo de espera mayor al que trae por defecto para evitar errores
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES) // read timeout
                .build();


        //Api a utilizar para consultar el clima
        String url = "https://api.openweathermap.org/data/2.5/weather?lat="+latitud+"&lon="+longitud+"&appid=affa18c5e2a87ebd663fffe73d41ef63";

        //Construimos un objeto para hacer la consulta
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {

            //Método por si hay un error
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            //Método que me devuelve la respuesta de la api en formato json
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if(response.isSuccessful()){ //Si la respuesta es satisfactoria
                    String myResponse = response.body().string();


                    //Ejecutamos un hilo para hacer la petición asincrona a la ejecución del programa
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                              //  JSONArray jsonObject = new JSONArray(myResponse);
                                //Deserealizamos el json
                                JSONObject json = new JSONObject(myResponse);
                                JSONObject json1 = json.getJSONObject("main");
                                JSONArray songsArray = json1.toJSONArray(json1.names());

                                //COnvertimos los valores que nos da en Kelvin a centigrados
                                String weather = kelvinToCelcius(songsArray.getString(0)) + "C°";
                                String weather_max = "Max: "+kelvinToCelcius(songsArray.getString(3)) + "C°";
                                String weather_min = "Min: "+kelvinToCelcius(songsArray.getString(2)) + "C°";

                                //Mostramos la información obtenida en textViews
                                clima.setText(weather);
                                clima_max.setText(weather_max);
                                clima_min.setText(weather_min);

                                JSONArray songsArray1 = json.toJSONArray(json.names());
                                JSONObject json2 = songsArray1.toJSONObject(songsArray1.getJSONArray(1));

                                JSONArray jArray3 = json.getJSONArray("weather");

                                //Obtenemos el tipo de icono
                                JSONObject object3;
                                String comp_id = "";
                                for(int i = 0; i < jArray3 .length(); i++)
                                {   
                                    object3 = jArray3.getJSONObject(i);
                                    comp_id = object3.getString("icon");
                                }

                                //Lo concatenamos a la url
                                String imageUrl = "https://openweathermap.org/img/wn/"+comp_id+"@2x.png";

                                //Mostramos la imagen cargada en la url en un imageView
                                Picasso.get()
                                        .load(imageUrl)
                                        .into(img);


                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }
        });

    }

    //Método para convertir de grados Kelvin a centigrados
    public String kelvinToCelcius(String grados){
        float farenheit = Float.parseFloat(grados);
        float resultado = (farenheit - 273.15f);
        resultado = (float) (Math.round(resultado * 100d) / 100d);
        //String f = String.valueOf(resultado);
        return String.valueOf(resultado);
    }
}