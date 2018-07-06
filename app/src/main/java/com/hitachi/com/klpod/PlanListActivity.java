package com.hitachi.com.klpod;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hitachi.com.klpod.Utility.DeviceInfo;
import com.hitachi.com.klpod.Utility.FuncDBAccess;
import com.hitachi.com.klpod.Utility.MasterAlert;
import com.hitachi.com.klpod.Utility.MasterServiceFunction;
import com.hitachi.com.klpod.Utility.PlanListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

public class PlanListActivity extends AppCompatActivity{


    MasterServiceFunction masterServiceFunction = new MasterServiceFunction();
    MasterAlert masterAlert =new MasterAlert(this);
    DeviceInfo deviceInfo =new DeviceInfo(this);
    String vehiclesCode ;
    String outBoundDate;
    String DeliveryNo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        vehiclesCode = getIntent().getStringExtra("VehiclesCode");

//        Create Toolbar
        createToolbar();

        //Get Outbound Date
        getOutboundDate();

        //Get Plan Detail
        getDelivery();

        //Check Button Start Job
        checkButtonStartJob();

        //Check Button End Job
        checkButtonEndJob();

        //Start Button Click
        startButtonClick();

        //end Button Click
        endButtonClick();

    }   // Main Method

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.itemExit) {

            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_plan_list, menu);

        return true;
    }

    private void createToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarPlanList);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_action_home);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();



    }

    private void endButtonClick() {
        Button endButton = findViewById(R.id.btnPLEndJob);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String LatitudeArrival = "-",LongitudeArrival = "-";
                deviceInfo.getLocation();
                LatitudeArrival = deviceInfo.getLatitude();
                LongitudeArrival = deviceInfo.getLongitude();
                JSONArray jsonArray = WebserviceExecute(masterServiceFunction.getUpdateEndJob()
                        +"/"+ DeliveryNo
                        +"/"+ LatitudeArrival
                        +"/"+ LongitudeArrival
                        +"/"+ vehiclesCode
                );
                Log.d("KLTag", "printStackTrace ==> " + jsonArray);
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    if( Boolean.valueOf(jsonObject.getString("Result")))
                    {
                        Toast.makeText(PlanListActivity.this, "Job Ended", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(PlanListActivity.this, "Job cannot end because :" + jsonObject.getString("MessageError"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("KLTag", "printStackTrace ==> " + e.getMessage());
                }
            }
        });
    }

    private void startButtonClick() {
        final Button StartJobButton = findViewById(R.id.btnPLStartJob);
        StartJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(PlanListActivity.this);
                builder.setCancelable(false);
                builder.setIcon(R.drawable.ic_action_alert);
                builder.setTitle("Confirm Start Job");
                builder.setMessage("Are You Sure ?");
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Start Job
                        startJob(StartJobButton);
                        dialog.dismiss();
                    }
                });
                builder.show();

            }   // onClick
        });
    }

    private void startJob(Button startJobButton) {
        String LatitudeDeparture = "-",LongitudeDeparture = "-";
        deviceInfo.getLocation();
        LatitudeDeparture = deviceInfo.getLatitude();
        LongitudeDeparture = deviceInfo.getLongitude();
        JSONArray jsonArray = WebserviceExecute(masterServiceFunction.getUpdateStartJob()
                +"/"+ DeliveryNo
                +"/"+ LatitudeDeparture
                +"/"+ LongitudeDeparture
                +"/"+ deviceInfo.IMEI()
                +"/"+ vehiclesCode
        );

        try {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if( Boolean.valueOf(jsonObject.getString("Result")))
            {
                startJobButton.setVisibility(View.GONE);
                Toast.makeText(PlanListActivity.this, "Job Started", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(PlanListActivity.this, "Job cannot start because :" + jsonObject.getString("MessageError"), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkButtonEndJob() {
        try {
            Button EndJobButton = findViewById(R.id.btnPLEndJob);
            JSONArray jsonArray = WebserviceExecute(masterServiceFunction.getCheckEndJob()
                    +"/"+ DeliveryNo);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if( Boolean.valueOf(jsonObject.getString("Result")))
                EndJobButton.setVisibility(View.VISIBLE);
            else
                EndJobButton.setVisibility(View.GONE);

            Log.d("KLTag", "EndJobButton getVisibility ==> " + EndJobButton.getVisibility());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONArray WebserviceExecute(String UrlFunc)
    {
        try {
            FuncDBAccess funcDBAccess = new FuncDBAccess(this);
            funcDBAccess.execute(UrlFunc);
            String resultJSON = funcDBAccess.SetJSONResult(funcDBAccess.get());
            JSONArray jsonArray = new JSONArray(resultJSON);

            return  jsonArray;
        } catch (Exception e)
        {
            e.printStackTrace();
            return  null;
        }

    }

    private void checkButtonStartJob() {
        try {
            Button StartJobButton = findViewById(R.id.btnPLStartJob);
            JSONArray jsonArray = WebserviceExecute(masterServiceFunction.getCheckStartJob()
                    +"/"+ DeliveryNo);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if( Boolean.valueOf(jsonObject.getString("Result")))
                StartJobButton.setVisibility(View.VISIBLE);
            else
                StartJobButton.setVisibility(View.GONE);

            Log.d("KLTag", "StartJobButton getVisibility ==> " + StartJobButton.getVisibility());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createPlanList(JSONArray jsonArray) {
        ListView listView = findViewById(R.id.lvPLPlanList);
        final String[] locationStrings = new String[jsonArray.length()];
        final String[] timeStrings = new String[jsonArray.length()];
        final String[] DeliveryDetailNoStrings = new String[jsonArray.length()];
        final String[] ownerCodeStrings = new String[jsonArray.length()];
        final String[] departureCheckStrings = new String[jsonArray.length()];

        try {
            for (int i=0;i<jsonArray.length();i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                locationStrings[i] = jsonObject.getString("StoreName");
                timeStrings[i] = jsonObject.getString("EstimateArrivalTime");
                DeliveryDetailNoStrings[i] = jsonObject.getString("DeliveryDetailNo");
                ownerCodeStrings[i] = jsonObject.getString("OwnerCode");
                departureCheckStrings[i] = jsonObject.getString("DepartureCheck");
            }
            Log.d("KLTag", "Location ==> " + locationStrings[0]);
            PlanListAdapter planListAdapter =new PlanListAdapter(this,locationStrings,timeStrings,departureCheckStrings);
            listView.setAdapter(planListAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //listView Item Click
                    Button StartJobButton = findViewById(R.id.btnPLStartJob);
                    if(!Boolean.valueOf(departureCheckStrings[position])) {
                        if (StartJobButton.getVisibility() != View.VISIBLE) {
                            Intent intent = new Intent(PlanListActivity.this, PlanDetailActivity.class);
                            intent.putExtra("DeliveryDetailNo", DeliveryDetailNoStrings[position]);
                            intent.putExtra("OwnerCode", ownerCodeStrings[position]);
                            intent.putExtra("VehiclesCode", vehiclesCode);
                            intent.putExtra("DeliveryNo", DeliveryNo);
                            startActivity(intent);
                        } else {
                            masterAlert.normalDialog("Warning", "Please press Start Job button.");
                        }
                    }
                    else
                    {
                        Toast.makeText(PlanListActivity.this, "This store has been shipped.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getDelivery() {
        try {
            JSONArray jsonArray = WebserviceExecute(masterServiceFunction.getGetDeliveryPlan()
                    +"/"+ vehiclesCode
                    +"/"+ outBoundDate);
            Log.d("KLTag", "Delivery ==> " + jsonArray);

            if(jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                TextView driverNameTextView = findViewById(R.id.txtPLDriverName);
                TextView carLicenseTextView = findViewById(R.id.txtPLCarLicense);
                TextView planCodeTextView = findViewById(R.id.txtPLPlanCode);

                String driverName = driverNameTextView.getText() + (jsonObject.getString("DriverName").equals("null") ? "" : jsonObject.getString("DriverName"));
                String carLicense = carLicenseTextView.getText() + (jsonObject.getString("VehiclesName").equals("null") ? "" : jsonObject.getString("VehiclesName"));
                String planCode = planCodeTextView.getText() + (jsonObject.getString("TripNo").equals("null") ? "" : jsonObject.getString("TripNo"));
                DeliveryNo = jsonObject.getString("DeliveryNo");

                driverNameTextView.setText(driverName);
                carLicenseTextView.setText(carLicense);
                planCodeTextView.setText(planCode);

                //Create Plan List
                createPlanList(jsonArray);
            }
            else
            {
                masterAlert.normalDialog("Warning","No plan for this truck!!");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getOutboundDate() {
        try {
            JSONArray jsonArray = WebserviceExecute(masterServiceFunction.getGetOutboundDate()
                    +"/"+ vehiclesCode);
            Log.d("KLTag", "OutboundDate ==> " + jsonArray);

            String[] outboundDateStrings = new String[jsonArray.length()];
            final String[] outboundDateFullFormatStrings = new String[jsonArray.length()];

            for (int i=0; i<jsonArray.length(); i+=1) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                outboundDateStrings[i] = jsonObject.getString("OutboundDate");
                outboundDateFullFormatStrings[i] = jsonObject.getString("OutboundDateFullFormat");
            }

            outBoundDate = outboundDateFullFormatStrings[0];

            Spinner spinner = findViewById(R.id.spinnerPLOutboundDate);
            ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(PlanListActivity.this,
                    android.R.layout.simple_list_item_1, outboundDateStrings);
            spinner.setAdapter(stringArrayAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    outBoundDate = outboundDateFullFormatStrings[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    outBoundDate = outboundDateFullFormatStrings[0];
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}

