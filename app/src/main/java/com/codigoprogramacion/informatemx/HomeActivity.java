package com.codigoprogramacion.informatemx;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.codigoprogramacion.informatemx.API.DiputadosAPI;
import com.codigoprogramacion.informatemx.API.InegiAPI;
import com.codigoprogramacion.informatemx.Helpers.XMLParser;


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import retrofit.RestAdapter;
import retrofit.client.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


public class HomeActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String key_value = "OBS_VALUE";
    public static final String key_time = "TIME_PERIOD";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private static XYSeries series;
    //private XYPlot plot;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.home, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements AdapterView.OnItemSelectedListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private JSONArray geo_cat_json;
        private JSONArray inegi_cat_json;
        private String selected_location;
        private String selected_data;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        private void reloadParams(){
            String name_data = ((Spinner)getActivity().findViewById(R.id.inegi_info_spinner)).getSelectedItem().toString();
            String name_loc = ((Spinner)getActivity().findViewById(R.id.inegi_geo_spinner)).getSelectedItem().toString();
            try{
                for(int i = 0; i< geo_cat_json.length();i++){
                    if(geo_cat_json.getJSONObject(i).getString("name").equals(name_loc))
                        selected_location = geo_cat_json.getJSONObject(i).getString("value");
                }

                for(int i = 0; i< inegi_cat_json.length();i++){
                    if(inegi_cat_json.getJSONObject(i).getString("name").equals(name_data))
                        selected_data = inegi_cat_json.getJSONObject(i).getString("number");
                }
            }catch (JSONException e){}

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);

            //Plot example
            LinearLayout lcontainer = (LinearLayout)rootView.findViewById(R.id.chart_container);

            series = new XYSeries("Catalogo");
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
            for(int i=0;i< 50;i++){
                series.add(i,Math.sin((float)i/3f));
            }
            dataset.addSeries(series);

            Spinner geo_cat = (Spinner)rootView.findViewById(R.id.inegi_geo_spinner);
            Spinner inegi_cat = (Spinner)rootView.findViewById(R.id.inegi_info_spinner);

            ArrayAdapter<CharSequence> geo_adapter = new ArrayAdapter<CharSequence>(getActivity(),android.R.layout.simple_spinner_item);
            ArrayAdapter<CharSequence> inegi_adapter = new ArrayAdapter<CharSequence>(getActivity(),android.R.layout.simple_spinner_item);

            try{
                StringWriter geoSw = new StringWriter();
                IOUtils.copy(getResources().openRawResource(R.raw.catalogo_inegi_geo),geoSw,"UTF-8");
                geo_cat_json = new JSONArray(geoSw.toString());
                geoSw.close();
                geoSw = null; geoSw = new StringWriter();
                IOUtils.copy(getResources().openRawResource(R.raw.catalogo_poblacion),geoSw,"UTF-8");
                inegi_cat_json = new JSONArray(geoSw.toString());
                geoSw.close();geoSw = null;

                for(int i = 0;i < geo_cat_json.length();i++){
                    System.out.println("Adding: "+geo_cat_json.getJSONObject(i).getString("name"));
                    geo_adapter.add(geo_cat_json.getJSONObject(i).getString("name"));
                }

                for(int i = 0;i < inegi_cat_json.length();i++){
                    System.out.println("Adding: " + inegi_cat_json.getJSONObject(i).getString("name"));
                    inegi_adapter.add(inegi_cat_json.getJSONObject(i).getString("name"));
                }

            }catch (IOException e){e.printStackTrace();}
            catch (JSONException e){}

            geo_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            geo_cat.setAdapter(geo_adapter);

            inegi_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            inegi_cat.setAdapter(inegi_adapter);

            rootView.findViewById(R.id.convocar_datos).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    System.out.println("CLICK!!");
                RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://www2.inegi.org.mx/servicioindicadores/Indicadores.asmx")//.setEndpoint("http://congresorest.appspot.com")
                .build();

                    InegiAPI service = restAdapter.create(InegiAPI.class);
                    //1002000001 poblacion total

                    StringWriter catalogo = new StringWriter();
                    try{
                        IOUtils.copy(getActivity().getResources().openRawResource(R.raw.catalogo_poblacion),
                                catalogo,"UTF-8");


                        final JSONArray catalogo_a = new JSONArray(catalogo.toString());
                        System.out.println("Catalogo poblacion total codigo:"+
                                catalogo_a.getJSONObject(0).getString("number"));

                        reloadParams();
                        System.out.println("data: "+selected_data+" "+selected_location);
                        service.getIndicadores(
                                selected_data,
                                selected_location,"1999","2014")
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        new Action1<Response>() {

                                            @Override
                                            public void call(Response response) {
                                                System.out.println("Status code:" + response.getStatus());
                                                try {
                                                    StringWriter writer = new StringWriter();
                                                    IOUtils.copy(response.getBody().in(), writer, "UTF-8");
                                                    System.out.println(writer.toString());
                                                    double min=0,max=0;
                                                    try {
                                                        JSONObject obj = XMLParser.toJSON(response.getBody().in());
                                                        JSONArray data = obj.getJSONArray("data");
                                                        //Plot example
                                                        LinearLayout lcontainer = (LinearLayout)getActivity().findViewById(R.id.chart_container);

                                                        series = new XYSeries("Catalogo");
                                                        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                                        for(int i=0;i<data.length();i++){
                                                            double value = data.getJSONObject(i).getInt(key_value);
                                                            series.add(data.getJSONObject(i).getInt(key_time),
                                                                    value);
                                                            if(i == 0){
                                                                min = value;max= value;
                                                            }else{
                                                                min = (value < min)?value:min;
                                                                max = (value > max)?value:max;
                                                            }



                                                        }
                                                        dataset.addSeries(series);

                                                        // Now we create the renderer
                                                        XYSeriesRenderer renderer = new XYSeriesRenderer();
                                                        renderer.setLineWidth(2);
                                                        renderer.setColor(Color.RED);
// Include low and max value

                                                        renderer.setDisplayBoundingPoints(true);
// we add point markers
                                                        renderer.setPointStyle(PointStyle.CIRCLE);
                                                        renderer.setPointStrokeWidth(3);

                                                        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
                                                        mRenderer.addSeriesRenderer(renderer);


// We want to avoid black border
                                                        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
// Disable Pan on two axis
                                                        mRenderer.setPanEnabled(false, false);
                                                        mRenderer.setYAxisMax(max*1.15);
                                                        mRenderer.setYAxisMin(min*0.85);
                                                        mRenderer.setShowGrid(true); // we show the grid


                                                        GraphicalView chartView = ChartFactory.getLineChartView(getActivity(),dataset, mRenderer);
                                                        // - See more at: htt
                                                        // p://www.survivingwithandroid.com/2014/06/android-chart-tutorial-achartengine.html#sthash.ZiGDS6PF.dpuf


                                                        lcontainer.addView(chartView,0);

                                                        //System.out.println("Obj..");
                                                        //System.out.println(obj.toString());
                                                    } catch (JSONException e) {
                                                    } catch (Exception e) {
                                                        System.out.println("superFail");
                                                    }


                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                );



                    }catch (IOException e){
                        e.printStackTrace();}
                    catch (JSONException e ){e.printStackTrace();}


                // closing comment */

                }
            });
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((HomeActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

}
