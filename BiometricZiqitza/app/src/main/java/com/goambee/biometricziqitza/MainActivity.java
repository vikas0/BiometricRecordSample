package com.goambee.biometricziqitza;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Precision.Component.FP.CommonAPI.BiometricComponent;
import com.Precision.Component.FP.CommonAPI.PrecisionCommonAPIErrorCodes;
import com.Precision.Component.FP.Global.PrecisionLogger;
import com.Precision.authapi.appcode.common.OutputDataType;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {


    private Button Capture;

    private Button clearData;

    public TextView TvImageQuality;
    public EditText TvCaptureAttempt;
    private ImageView Fpimage;
    boolean isPermissionAvailable = false;
    byte[] certificate = null;
    byte[] byteTemplate;
    boolean captureCompleted = false;
    private boolean bDeviceStatus = false;

    private BiometricComponent biometricComponent = new BiometricComponent();

    private String ACTION_USB_PERMISSION = "com.precision.commonAPI.USB_PERMISSION";
    private boolean bDevicePermission = false;

    private int result = -1;
    private byte[] raw = null;
    private int h, w, quality = 0;
    private Bitmap bmp;
    private byte[] isoTemplate;

    private int NFIQ;
    private String PIDData = null;
    private String HMacData = null;
    private String SessionKey = null;
    private String TimeStamp = null;
    private String CertExpiryDate = null;
    private String SerialNumber = null;

    PrintWriter out = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Capture = (Button) findViewById(R.id.btnStartCapture);

        clearData = (Button) findViewById(R.id.clearAttempt);

        TvCaptureAttempt = (EditText) findViewById(R.id.CaptureAttemptTextView);

        Fpimage = (ImageView) findViewById(R.id.fp_image);

        Capture.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                try {
                    Capture.setEnabled(false);
                    checkScanner();

                    if (bDeviceStatus) {
                        if (isPermissionAvailable) {
                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            Fpimage.setImageBitmap(null);

                                        }
                                    });
                                    String timestamp = GetUTCdatetimeAsString();

                                    biometricComponent
                                            .SetApplicationContext(MainActivity.this);
                                    biometricComponent.setTimeStamp(timestamp);
                                    biometricComponent.FindConnectedDevice();
                                    biometricComponent.EnableLog(1);
                                    biometricComponent.setCaptureTimeOut(15000);
                                    biometricComponent.setTemplateTrimingCount(65);
                                    biometricComponent.set2FA(true);
                                    int captAttempt = 0; // or other invalid
                                    // value
                                    if (TvCaptureAttempt.getText().toString()
                                            .length() > 0)
                                        captAttempt = Integer
                                                .parseInt(TvCaptureAttempt
                                                        .getText().toString());
                                    biometricComponent
                                            .setCaptureAttempt(captAttempt);

                             /*todo -where is  "uidai_auth_stage.cer" */
                                    try {
                                        InputStream in = getAssets().open(
                                                "uidai_auth_stage.cer");
                                        certificate = new byte[in.available()];
                                        in.read(certificate);
                                        in.close();
                                        if (certificate != null) {
                                            biometricComponent
                                                    .setUIDAIEncryptionRequired(true);
                                            biometricComponent
                                                    .setUidaiCert(certificate);

                                        }
                                    } catch (IOException e1) {

                                        e1.printStackTrace();
                                    }

                                    biometricComponent
                                            .setOutputdatatype(OutputDataType.PROTOBUFF);

                                    result = biometricComponent.initDevice();
                                    if ((result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_SUCCESS)) {


                                        result = biometricComponent.FPCapture();

                                        if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_SUCCESS) {

                                            raw = biometricComponent
                                                    .getRawImageData();
                                            System.out
                                                    .println("Input TimeStamp:"
                                                            + timestamp);

                                            h = biometricComponent
                                                    .getImageHeight();
                                            w = biometricComponent
                                                    .getImageWidth();

                                            isoTemplate = biometricComponent
                                                    .getISOTemplate();

                                            quality = biometricComponent
                                                    .getImageQuality();
											/*
											 * runOnUiThread(new Runnable() {
											 *
											 * @Override public void run() {
											 *
											 * TvImageQuality.setText(quality);
											 *
											 * } });
											 */
                                            NFIQ = biometricComponent.getNFIQ();
											/*
											 * runOnUiThread(new Runnable() {
											 *
											 * @Override public void run() {
											 *
											 * TvNfiq.setText(NFIQ);
											 *
											 * } });
											 */
                                            PIDData = biometricComponent
                                                    .getPIDData();

											/*
											 * File dir = new
											 * File("/sdcard/PIDblock"); if
											 * (!dir.exists()) { dir.mkdir(); }
											 *
											 * try { PrintWriter out = new
											 * PrintWriter
											 * ("/sdcard/PIDblock/PIDdata_"
											 * +captAttempt+".txt");
											 * out.print(PIDData); out.close();
											 *
											 * } catch (Exception e) {
											 * e.printStackTrace(); }
											 */
                                            HMacData = biometricComponent
                                                    .getHMacData();
											/*
											 * try { PrintWriter out = new
											 * PrintWriter
											 * ("/sdcard/PIDblock/HMacData_"
											 * +captAttempt+".txt");
											 * out.print(HMacData); out.close();
											 *
											 * } catch (Exception e) {
											 * e.printStackTrace(); }
											 */
                                            SessionKey = biometricComponent
                                                    .getSessionKey();
											/*
											 * try { PrintWriter out = new
											 * PrintWriter
											 * ("/sdcard/PIDblock/SessionKey_"
											 * +captAttempt+".txt");
											 * out.print(SessionKey);
											 * out.close();
											 *
											 * } catch (Exception e) {
											 * e.printStackTrace(); }
											 */
                                            TimeStamp = biometricComponent
                                                    .getTimeStamp();
                                            System.out
                                                    .println("Output TimeStamp:"
                                                            + TimeStamp);
											/*
											 * try { PrintWriter out = new
											 * PrintWriter
											 * ("/sdcard/PIDblock/TimeStamp_"
											 * +captAttempt+".txt");
											 * out.print(TimeStamp);
											 * out.close();
											 *
											 * } catch (Exception e) {
											 * e.printStackTrace(); }
											 */
                                            CertExpiryDate = biometricComponent
                                                    .getCertExpiryDate();
											/*
											 * try { PrintWriter out = new
											 * PrintWriter
											 * ("/sdcard/PIDblock/CertExpiryDate_"
											 * +captAttempt+".txt");
											 * out.print(CertExpiryDate);
											 * out.close();
											 *
											 * } catch (Exception e) {
											 * e.printStackTrace(); }
											 */
                                            SerialNumber = biometricComponent
                                                    .getSerialNumber();
                                            runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    Toast.makeText(
                                                            MainActivity.this,
                                                            "Serial Number : "
                                                                    + SerialNumber,
                                                            Toast.LENGTH_SHORT)
                                                            .show();

                                                }
                                            });
                                            bmp = biometricComponent
                                                    .getBitmapImage();

                                            runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    Fpimage.setImageBitmap(bmp);

                                                    Toast.makeText(
                                                            MainActivity.this,
                                                            "Capture Fingerprint Success",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    Capture.setEnabled(true);

                                                }
                                            });
                                            result = biometricComponent
                                                    .UnInitDevice();
                                            Log.w("uninitiate", "result :"
                                                    + result);

                                        } else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_FINGER_ALREADY_GIVEN) {
                                            runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    Toast.makeText(
                                                            MainActivity.this,
                                                            "finger is already given",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    biometricComponent
                                                            .UnInitDevice();
                                                    Capture.setEnabled(true);
                                                }
                                            });
                                        } else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_INVALID_CAPTURE_ATTEMPT) {
                                            runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    Toast.makeText(
                                                            MainActivity.this,
                                                            "Invalid Capture Attempt",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    biometricComponent
                                                            .UnInitDevice();
                                                    Capture.setEnabled(true);
                                                }
                                            });
                                        } else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_EXTRACTION_FAILED) {
                                            runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    Toast.makeText(
                                                            MainActivity.this,
                                                            "Extraction failed",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    biometricComponent
                                                            .UnInitDevice();
                                                    Capture.setEnabled(true);
                                                }
                                            });
                                        } else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_NO_SCANNER_FOUND) {
                                            runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    Toast.makeText(
                                                            MainActivity.this,
                                                            "No Scanner found",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    biometricComponent
                                                            .UnInitDevice();
                                                    Capture.setEnabled(true);
                                                }
                                            });
                                        } else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_IMAGE_CAPTURE_FAILED) {
                                            runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    Toast.makeText(
                                                            MainActivity.this,
                                                            "Image capture failed",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    biometricComponent
                                                            .UnInitDevice();
                                                    Capture.setEnabled(true);
                                                }
                                            });

                                        } else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_FINGERPRINT_IMAGE_CAPTURE_TIMEOUT) {
                                            runOnUiThread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    Toast.makeText(
                                                            MainActivity.this,
                                                            "capture time out",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    biometricComponent
                                                            .UnInitDevice();
                                                    Capture.setEnabled(true);
                                                }
                                            });

                                        } else {
                                            result = biometricComponent
                                                    .UnInitDevice();
                                            Log.w("uninitiate", "result :"
                                                    + result);
                                            Capture.setEnabled(true);
                                        }

                                    }

                                    else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_INIT_LICENSE_FAILED) {
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                Toast.makeText(
                                                        MainActivity.this,
                                                        "License failed",
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                                Capture.setEnabled(true);
                                            }
                                        });

                                    }

                                    else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_NO_SCANNER_FOUND) {
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                Toast.makeText(
                                                        MainActivity.this,
                                                        "No Scanner found",
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                                Capture.setEnabled(true);
                                            }
                                        });

                                    } else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_DEVICE_NOT_COMPATIBLE) {
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                Toast.makeText(
                                                        MainActivity.this,
                                                        "Device not compatible",
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                                Capture.setEnabled(true);
                                            }

                                        });

                                    } else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_SCANNER_INITILIZATION_FAILED) {
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                Toast.makeText(
                                                        MainActivity.this,
                                                        "Device init failed",
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                                Capture.setEnabled(true);
                                            }
                                        });

                                    } else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_SCANNER_PERMISSION_NOT_GRANTED) {
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                Toast.makeText(
                                                        MainActivity.this,
                                                        "Device has no permission to access",
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                                Capture.setEnabled(true);
                                            }
                                        });

                                    } else if (result == PrecisionCommonAPIErrorCodes.PB_ERRORCODE_SCANNER_ALREADY_INITIALIZED) {
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                Toast.makeText(
                                                        MainActivity.this,
                                                        "Device already initialized",
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });

                                    } else {
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                Toast.makeText(
                                                        MainActivity.this,
                                                        "Device Init Failed",
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                                Capture.setEnabled(true);
                                            }
                                        });

                                    }

                                    // captureCompleted=false;
                                    // raw=null;
                                    // h=0;
                                    // w=0;
                                    // quality=0;
                                    // bmp=null;

                                }
                            }).start();

                        } else {

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,
                                            "Device has no permission",
                                            Toast.LENGTH_SHORT).show();
                                    Capture.setEnabled(true);
                                }
                            });

                        }
                    } else {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "No Scanner found", Toast.LENGTH_SHORT)
                                        .show();
                                Capture.setEnabled(true);

                            }
                        });

                    }
                } catch (Exception e) {

                    e.printStackTrace();

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Exception",
                                    Toast.LENGTH_SHORT).show();
                            Capture.setEnabled(true);
                        }
                    });

                } finally {
                    Capture.setClickable(true);
                }
            }
        });

        clearData.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                biometricComponent.clearData();
            }
        });

    }

    @SuppressLint("NewApi")
    public void checkScanner() {
        // bDeviceStatus=false;
        try {
            UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
                    MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0);
            // HashMap deviceList = manager.getDeviceList();
            Iterator deviceIterator = manager.getDeviceList().values()
                    .iterator();
            while (deviceIterator.hasNext()) {
                UsbDevice device = (UsbDevice) deviceIterator.next();
                if (device.getProductId() == 8214
                        || device.getProductId() == 30264
                        || 0x2109 == device.getVendorId()
                        &&
                        0x7638 == device.getProductId()
                        || 0x2D38 == device.getVendorId()
                        &&
                        0x07D0 == device.getProductId()
                        || 0x2D38 == device.getVendorId()
                        && 2010 == device.getProductId()) {
                    bDeviceStatus = true;
                    {
                        if (!manager.hasPermission(device)) {
                            isPermissionAvailable = false;
                            manager.requestPermission(device, mPermissionIntent);
                        } else {
                            isPermissionAvailable = true;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @SuppressLint("NewApi")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                try {
                    PrecisionLogger
                            .Write("MainActivity=>BroadcastReceiver=> Device Attached",
                                    1);
                    UsbManager mUsbManager = (UsbManager) getSystemService(USB_SERVICE);
                    for (UsbDevice device : mUsbManager.getDeviceList()
                            .values()) {
                        if (device.getProductId() == 8214
                                || device.getProductId() == 30264
                                || 0x2109 == device.getVendorId()
                                && 0x7638 == device.getProductId()
                                || 0x2D38 == device.getVendorId()
                                && 0x07D0 == device.getProductId()
                                || 0x2D38 == device.getVendorId()
                                && 2010 == device.getProductId()) {
                            if (!mUsbManager.hasPermission(device)) {
                                PendingIntent mPermissionIntent = PendingIntent
                                        .getBroadcast(MainActivity.this, 0,
                                                new Intent(
                                                        ACTION_USB_PERMISSION),
                                                0);
                                mUsbManager.requestPermission(device,
                                        mPermissionIntent);
                                bDevicePermission = false;
                            } else {
                                bDevicePermission = true;

                            }
                            break;
                        }
                    }

                } catch (Exception Ex) {
                    PrecisionLogger.Write(
                            "MainActivity=>BroadcastReceiver=>Exception occred in Attached event:"
                                    + Ex.getMessage().toString(), 1);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                PrecisionLogger.Write(
                        "MainActivity=>BroadcastReceiver=> Device Detached", 1);
            }
        }
    };

    public String GetUTCdatetimeAsString() {

        String utcTime = null;
        String utcTime2 = null;
        String strTimeStamp = null;
        StringBuilder s = new StringBuilder();

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat f1 = new SimpleDateFormat("HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        f1.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.out.println();
        utcTime = f.format(new Date());
        utcTime2 = f1.format(new Date());
        s.append(utcTime);
        s.append("T");
        s.append(utcTime2);
        strTimeStamp = s.toString();

        return strTimeStamp;
    }

    // For Marshmallow permission

	/*
	 * @SuppressLint("NewApi") @Override public void
	 * onRequestPermissionsResult(int requestCode, String[] permissions, int[]
	 * grantResults) { super.onRequestPermissionsResult(requestCode,
	 * permissions, grantResults); switch (requestCode) { case
	 * REQUEST_WRITE_STORAGE: { if (grantResults.length > 0 && grantResults[0]
	 * == PackageManager.PERMISSION_GRANTED) {
	 * Toast.makeText(getApplicationContext(), "Permission granted to read",
	 * Toast.LENGTH_LONG).show();
	 *
	 * //reload my activity with permission granted or use the features what
	 * required the permission } else { Toast.makeText(getApplicationContext(),
	 * "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission"
	 * , Toast.LENGTH_LONG).show(); } } case REQUEST_WIFI_STATE: { if
	 * (grantResults.length > 0 && grantResults[0] ==
	 * PackageManager.PERMISSION_GRANTED) {
	 * Toast.makeText(getApplicationContext(),
	 * "Permission granted for wifi state", Toast.LENGTH_LONG).show();
	 *
	 * //reload my activity with permission granted or use the features what
	 * required the permission } else { Toast.makeText(getApplicationContext(),
	 * "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission"
	 * , Toast.LENGTH_LONG).show(); } } case REQUEST_INTERNET: { if
	 * (grantResults.length > 0 && grantResults[0] ==
	 * PackageManager.PERMISSION_GRANTED) {
	 * Toast.makeText(getApplicationContext(),
	 * "Permission granted for wifi state", Toast.LENGTH_LONG).show();
	 *
	 * //reload my activity with permission granted or use the features what
	 * required the permission } else { Toast.makeText(getApplicationContext(),
	 * "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission"
	 * , Toast.LENGTH_LONG).show(); } } case REQUEST_NETWORK_STATE: { if
	 * (grantResults.length > 0 && grantResults[0] ==
	 * PackageManager.PERMISSION_GRANTED) {
	 * Toast.makeText(getApplicationContext(),
	 * "Permission granted for wifi state", Toast.LENGTH_LONG).show();
	 *
	 * //reload my activity with permission granted or use the features what
	 * required the permission } else { Toast.makeText(getApplicationContext(),
	 * "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission"
	 * , Toast.LENGTH_LONG).show(); } } }
	 *
	 * }
	 */
}


