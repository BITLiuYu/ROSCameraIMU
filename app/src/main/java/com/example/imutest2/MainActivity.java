package com.example.imutest2;

import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.WindowDecorActionBar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.android.view.camera.RosCameraPreviewView;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.io.IOException;

//RosActivity is getting from Android-ROS interface
//来源：https://blog.csdn.net/m0_37140773/article/details/78561452
public class MainActivity extends RosActivity {
    //相机
    private RosCameraPreviewView rosCameraPreviewView;
    //private Camera mCamera;

    //IMU
    private ImuPublisher.SensorListener msensorListener;
    private ImuPublisher imu_pub;
    private SensorManager msensorManager;
    private EditText et;
    public static Handler handler;
    public static long start_time;
    public MainActivity(){
        super("MainActivity","MainActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        start_time=System.currentTimeMillis();
        et=(EditText)findViewById(R.id.et);
        //获取系统的传感器管理服务
        msensorManager=(SensorManager)this.getSystemService(SENSOR_SERVICE);
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle=msg.getData();
                et.setText(bundle.get("result").toString());
                super.handleMessage(msg);
            }
        };


    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    //是从父类继承过来的，父类再启动中会startServic

    //This method is called in a background thread once this Activity has been initialized with a master URI via the MasterChooser and a NodeMainExecutorService has started.
    // Your NodeMains should be started here using the provided NodeMainExecutor.    //
    //Parameters:
    //    nodeMainExecutor	the NodeMainExecutor created for this Activity
    @Override
    protected void init(final NodeMainExecutor nodeMainExecutor) {

        //几个节点就定义几个nodeconfigration
        NodeConfiguration nodeConfiguration1 =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                        getMasterUri());
        NodeConfiguration nodeConfiguration2 =
                NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(),
                        getMasterUri());
        //相机
        this.rosCameraPreviewView = (RosCameraPreviewView) findViewById(R.id.cam_layout);
        rosCameraPreviewView.setCamera(getCamera());

        nodeConfiguration1.setNodeName("Camera_Test");
        //Executes the supplied NodeMain using the supplied NodeConfiguration.
        //Parameters:
        //      nodeMain	the NodeMain to execute
        //      nodeConfiguration	the NodeConfiguration that will be used to create the Node
        nodeMainExecutor.execute(this.rosCameraPreviewView, nodeConfiguration1);

        //IMU
        this.imu_pub=new ImuPublisher(msensorManager);
        nodeConfiguration2.setNodeName("IMU_Test");
        nodeMainExecutor.execute(this.imu_pub,nodeConfiguration2);
        msensorListener=imu_pub.sensorListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
    @Override
    protected void onStop() {
        // unregister listener
        msensorManager.unregisterListener(msensorListener);
        super.onStop();
    }

    //设置相机
    private Camera getCamera(){
        Camera camera=Camera.open(0);
        Camera.Parameters parameters=camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        //parameters.set("orientation", "landscape");//这样设置也可以
        //camera.setDisplayOrientation(90);//相机旋转90度，便成竖屏
        camera.setParameters(parameters);
        return camera;

    }
}

