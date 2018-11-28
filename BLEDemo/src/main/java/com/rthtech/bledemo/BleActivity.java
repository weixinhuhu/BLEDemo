package com.rthtech.bledemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.rthtech.ble.Controller;
import com.rthtech.ble.Data;
import static com.rthtech.bledemo.Util.AddSpace;

public class BleActivity extends Activity implements OnItemClickListener,
        com.rthtech.ble.Callback, OnClickListener {
    //窗体显示相关变量
    private final static String TAG = "BLE";
    private final static String STR_LINE_SESSION = "===========================";
    private final static String STR_LINE_COMMAND = "--------------------";
    private List<Map<String, String>> mListAPI = null;
    private ArrayAdapter<String> mAdapterLog = null;
    private ArrayList<String> mListLog = null;
    private ListView mListViewLog = null;
    private View mDialogView = null;
    private List<String> mListDeviceName = null;
    private List<String> mListDeviceAddress = null;
    private boolean mScanMode = false;
    private InputData mInputData = new InputData();
    public boolean mTestCardId;

    // 卡片操作相关变量
    public String CLA;
    public String INS;
    public String P1;
    public String P2;
    public String LC;
    public String DATE;
    public String LE;
    public String errmessage = "";
    public String masterkey = "";
    public String PublicKey = "";
    public String SignValue = "";
    public String StrApdu = "";

    // 卡片操作记时变量
    public long begintime;
    public long endtime;
    public long costTime;
    // 返回标识
    public int flag = -1;

    public BleActivity() {
    }

    private class MyNumberKeyListener extends NumberKeyListener {
        private MyNumberKeyListener(boolean hex) {
            this.hex = hex;
        }

        private char[] hexChars = "0123456789abcdefABCDEF".toCharArray();
        private boolean hex;
        private char[] ascChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()-=_+`~,./<>?[]{}\\|\'\" "
                .toCharArray();

        @Override
        public int getInputType() {
            return android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        }

        @Override
        protected char[] getAcceptedChars() {
            if (hex) return hexChars;
            return ascChars;
        }
    }

    private static class InputData {
        private boolean boolValue;
        private byte byteValue1;
        private byte byteValue2;
        private byte[] dataValue1;
        public int dataLength1;
        private String title;
        private String labelBool;
        private String labelByte1;
        private String labelByte2;
        private String labelData1;
        public boolean asciiData1;

        private InputData() {
            reset();
        }

        private void reset() {
            title = null;
            labelBool = null;
            labelByte1 = null;
            labelByte2 = null;
            labelData1 = null;
            boolValue = false;
            byteValue1 = ( byte ) 0;
            byteValue2 = ( byte ) 0;
            dataValue1 = null;
            asciiData1 = false;
        }

        private void setDataLength(int length) {
            dataLength1 = length;
        }

        private void set(String title, String labelBool, boolean boolValue,
                         String labelByte1, byte byteValue1, String labelData1,
                         byte[] dataValue1) {
            reset();
            this.title = title;
            this.labelBool = labelBool;
            this.boolValue = boolValue;
            this.labelByte1 = labelByte1;
            this.byteValue1 = byteValue1;
            this.labelData1 = labelData1;
            this.dataValue1 = dataValue1;
        }

        private void set(String title, String labelByte1, byte byteValue1,
                         String labelData1, byte[] dataValue1) {
            reset();
            this.title = title;
            this.labelByte1 = labelByte1;
            this.byteValue1 = byteValue1;
            this.labelData1 = labelData1;
            this.dataValue1 = dataValue1;

        }

        private void set(String title, String labelBool, boolean boolValue) {
            reset();
            this.title = title;
            this.labelBool = labelBool;
            this.boolValue = boolValue;
        }

        private void set(String title, String labelByte1, byte byteValue1,
                         String labelByte2, byte byteValue2) {
            reset();
            this.title = title;
            this.labelByte1 = labelByte1;
            this.byteValue1 = byteValue1;
            this.labelByte2 = labelByte2;
            this.byteValue2 = byteValue2;
        }

        private void set(String title, String labelByte1, byte byteValue1) {
            reset();
            this.title = title;
            this.labelByte1 = labelByte1;
            this.byteValue1 = byteValue1;
        }

        private void set(String title, String labelData1, byte[] dataValue1,
                         boolean ascii) {
            reset();
            this.title = title;
            this.labelData1 = labelData1;
            this.dataValue1 = dataValue1;
            this.asciiData1 = ascii;
        }
    }

    private int mCommand;

    private Controller mController = null;

    enum APIId {
        AntennaControl, SelectCard, LoginSector, ExchangeTransparentData1, ExchangeTransparentData2, ExchangeTransparentData3, ExchangeTransparentData4, ExchangeTransparentData6, ExchangeTransparentData5, PairDevice
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAPIList();

        LogcatHelper.getInstance(this).start();


        SimpleAdapter mAdapterAPI = new SimpleAdapter(this, mListAPI,
                R.layout.simple_list_item_2, new String[]{"name", "desc"},
                new int[]{android.R.id.text1, android.R.id.text2});
        ListView mListViewAPI = findViewById(R.id.listViewAPI);
        mListViewAPI.setAdapter(mAdapterAPI);
        mListViewAPI.setOnItemClickListener(this);

        mListLog = new ArrayList<>();
        mAdapterLog = new ArrayAdapter<>(this,
                R.layout.simple_list_item_1, mListLog);
        mListViewLog = findViewById(R.id.listViewLog);
        if (null != mListViewLog) mListViewLog.setAdapter(mAdapterLog);

        mListDeviceName = new ArrayList<>();
        mListDeviceAddress = new ArrayList<>();
        mScanMode = false;

        Button btn;

        btn = findViewById(R.id.btnScan);
        btn.setOnClickListener(this);
        btn.setText(R.string.scan);

        btn = findViewById(R.id.btnStart);
        btn.setOnClickListener(this);
        btn.setText(R.string.connect);
        btn.setEnabled(false);

        btn = findViewById(R.id.btnPair);
        btn.setOnClickListener(this);
        btn.setText(R.string.pair);
        btn.setEnabled(false);

        btn = findViewById(R.id.btnCardId);
        btn.setOnClickListener(this);
        btn.setText(R.string.card_id);
        btn.setEnabled(false);

        btn = findViewById(R.id.btnClear);
        btn.setOnClickListener(this);
        btn.setText(R.string.clear);

        mController = com.rthtech.ble.Factory.getController(this);
        mController.init(this);
        if (!mController.isBluetoothEnabled()) {
            mController.term();
            String text;
            text = this.getResources().getString(R.string.bthdisabled);
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mController.term();
    }

    private void setText(int id, int resid) {
        TextView tv = findViewById(id);
        if (null != tv) tv.setText(resid);
    }

    private void enableView(int id, boolean enabled) {
        View v = findViewById(id);
        if (null != v) v.setEnabled(enabled);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    private void initAPIList() {
        Map<String, String> item;
        if (null == mListAPI) {
            mListAPI = new ArrayList<>();
            item = new HashMap<>();
            item.put("name", "开关场");
            item.put("desc", "args: true/false - on/off");
            item.put("id", "" + APIId.AntennaControl.ordinal());
            mListAPI.add(item);

            item = new HashMap<>();
            item.put("name", "寻卡");
            item.put("desc", "no args");
            item.put("id", "" + APIId.SelectCard.ordinal());
            mListAPI.add(item);

            item = new HashMap<>();
            item.put("name", "生成seed");
            item.put("id", "" + APIId.ExchangeTransparentData1.ordinal());
            mListAPI.add(item);

            item = new HashMap<>();
            item.put("name", "衍生公私钥-比特币");
            item.put("id", "" + APIId.ExchangeTransparentData2.ordinal());
            mListAPI.add(item);

            item = new HashMap<>();
            item.put("name", "数字签名-比特币");
            item.put("id", "" + APIId.ExchangeTransparentData3.ordinal());
            mListAPI.add(item);

            item = new HashMap<>();
            item.put("name", "衍生公私钥-以太坊");
            item.put("id", "" + APIId.ExchangeTransparentData4.ordinal());
            mListAPI.add(item);

            item = new HashMap<>();
            item.put("name", "数字签名-以太坊");
            item.put("id", "" + APIId.ExchangeTransparentData5.ordinal());
            mListAPI.add(item);

            item = new HashMap<>();
            item.put("name", "seed恢复");
            item.put("id", "" + APIId.ExchangeTransparentData6.ordinal());
            mListAPI.add(item);
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        byte[] key = {( byte ) 0xff, ( byte ) 0xff, ( byte ) 0xff, ( byte ) 0xff,
                ( byte ) 0xff, ( byte ) 0xff};
        byte[] data = {( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
                ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0,
                ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0};
        byte[] page = {( byte ) 0, ( byte ) 0, ( byte ) 0, ( byte ) 0};

        int obj;
        Map<String, String> item;

        item = mListAPI.get(position);

        obj = Integer.parseInt(item.get("id"));

        mCommand = obj;
        // AntennaControl
        if (obj == APIId.AntennaControl.ordinal())
            showDataUI(APIId.AntennaControl, "Antenna on or off", true);
        else if (obj == APIId.SelectCard.ordinal()) {
            // SelectCard
            if (checkSessionStatus()) return;
            if (mController.isBusy()) {
                log("Another command is running!");
                return;
            }
            log(APIId.SelectCard);
            mController.selectCard();
        } else if (obj == APIId.LoginSector.ordinal()) {
            // LoginSector
            setDataLength(6);
            showDataUI(APIId.LoginSector, "Master Key", true, "Sector Number",
                    ( byte ) 1, "Key (6 bytes)", key);

        } else if (obj == APIId.ExchangeTransparentData1.ordinal()) {
            // 生成masterkey
            flag = 1;
            try {
                CreatSeed("11223344556677881122334455667788");
            } catch (Exception e) {
                log(e.toString());
            }

        } else if (obj == APIId.ExchangeTransparentData2.ordinal()) {
            // 衍生公私鑰對-比特幣
            flag = 2;
            getPublicKey("00", "10000002");
        } else if (obj == APIId.ExchangeTransparentData3.ordinal()) {
            // 簽名-比特幣
            flag = 3;
            getSignature(
                    "00",
                    "10000002",
                    "112233445566778811223344556677881122334455667788112233445566778811223344556677881122334455667788112233445566778811223344556677881122334455667788112233445566778811223344556677881122334455667788");
        } else if (obj == APIId.ExchangeTransparentData4.ordinal()) {
            // 衍生公私鑰對-以太坊
            flag = 4;
            getPublicKey("3C", "00000002");
        } else if (obj == APIId.ExchangeTransparentData5.ordinal()) {
            // 簽名-以太坊
            flag = 5;
            getSignature(
                    "3C",
                    "00000002",
                    "112233445566778811223344556677881122334455667788112233445566778811223344556677881122334455667788112233445566778811223344556677881122334455667788112233445566778811223344556677881122334455667788");
        } else if (obj == APIId.ExchangeTransparentData6.ordinal()) {
            // 恢復seed
            flag = 6;
            recoveryKey("11223344556677881122334455667788", masterkey);
        }
    }

    /**
     * 生成Seed
     *
     * @param key 用户输入的支付密码，长度任意
     */
    public void CreatSeed(String key) {
        CLA = "80";
        INS = "E0";
        P1 = "00";
        P2 = "00";
        LC = "12";
        DATE = Objects.requireNonNull(Utils.getSha1(key)).substring(0, 32)
                + CRCUtil.getCrc16(Objects.requireNonNull(Utils.getSha1(key)).substring(0, 32));
        LE = "44";
        StrApdu = CLA + INS + P1 + P2 + LC + DATE + LE;
        log("发送==>>" + AddSpace(StrApdu));

        final byte[] Apdudata = Util.hexStringToByte(StrApdu.toUpperCase()
                .replace(" ", ""));
        begintime = System.currentTimeMillis();
        mController.exchangeTransparentData(Apdudata, Apdudata.length);
    }

    /**
     * 获取公钥
     *
     * @param coinType  `00`表示?特币；`3C`表示以太坊
     * @param accountID 钱包ID 4字节
     */
    public void getPublicKey(String coinType, String accountID) {
        CLA = "80";
        INS = "E2";
        P1 = "00";
        P2 = coinType; // `00`表示?比特币；`3C`表示以太坊
        LC = "04";
        DATE = accountID;
        LE = "44";
        StrApdu = CLA + INS + P1 + P2 + LC + DATE + LE;
        log("发送==>>" + AddSpace(StrApdu));
        final byte[] Apdudata = Util.hexStringToByte(StrApdu.toUpperCase()
                .replace(" ", ""));
        begintime = System.currentTimeMillis();
        mController.exchangeTransparentData(Apdudata, Apdudata.length);
    }

    /**
     * 签名
     *
     * @param coinType        `00`表示?比特币；`3C`表示以太坊
     * @param accountID       钱包ID
     * @param hashTransaction 待签名的Hash值
     */
    public void getSignature(String coinType, String accountID,
                             String hashTransaction) {
        CLA = "80";
        INS = "E4";
        P1 = "00";
        P2 = coinType; // `00`表示?比特币；`3C`表示以太坊
        LC = "44";
        DATE = accountID + hashTransaction;
        LE = "44";
        StrApdu = CLA + INS + P1 + P2 + LC + DATE + LE;
        log("发送==>>" + AddSpace(StrApdu));
        final byte[] Apdudata = Util.hexStringToByte(StrApdu.toUpperCase()
                .replace(" ", ""));
        begintime = System.currentTimeMillis();
        mController.exchangeTransparentData(Apdudata, Apdudata.length);
    }

    /**
     * seed恢复
     *
     * @param key            用户支付密码
     * @param seedCiphertext seed密文
     */
    public void recoveryKey(String key, String seedCiphertext) {
        CLA = "80";
        INS = "E6";
        P1 = "00";
        P2 = "00";
        LC = "50";
        DATE = key + seedCiphertext;
        LE = "04";
        StrApdu = CLA + INS + P1 + P2 + LC + DATE + LE;
        log("发送==>>" + AddSpace(StrApdu));
        final byte[] Apdudata = Util.hexStringToByte(StrApdu.toUpperCase()
                .replace(" ", ""));
        begintime = System.currentTimeMillis();
        mController.exchangeTransparentData(Apdudata, Apdudata.length);
    }

    private boolean checkSessionStatus() {
        if (null == mController) {
            log("Service is not ready!");
            return true;
        }
        if (!mController.isReady()) {
            log("No device connected!");
            return true;
        }
        return false;
    }

    public void log(String str) {
        if (null != mListViewLog) {
            if (mListLog.size() >= 100) mListLog.remove(0);
            mListLog.add(str);
            mAdapterLog.notifyDataSetChanged();
            mListViewLog.smoothScrollToPosition(mListLog.size() - 1);
        }
        Log.d(TAG, str);

    }

    private void log(APIId cmd) {
        log(cmd.toString());
    }

    @SuppressWarnings("unused")
    private void execCommand(int command) {
        byte byte1, byte2;
        boolean bool;
        byte[] data1;
        int obj;

        obj = command;
        bool = mInputData.boolValue;
        data1 = mInputData.dataValue1;

        if (checkSessionStatus()) return;
        if (mController.isBusy()) {
            log("Another command is running!");
            return;
        }
        if (obj == APIId.AntennaControl.ordinal()) {
            log(APIId.AntennaControl);
            mController.antennaControl(bool);
        } else // impossible
            if (obj == APIId.SelectCard.ordinal()) mController.selectCard();
            else if (obj == APIId.PairDevice.ordinal()) {
                log(APIId.PairDevice);
                mController.pairDevice(data1);
            } else if (obj == APIId.ExchangeTransparentData1.ordinal()) {
                log(APIId.ExchangeTransparentData1);
                mController.exchangeTransparentData(data1, data1.length);
            }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnScan) onButtonScan();
        else if (v.getId() == R.id.btnStart) onButtonStart();
        else if (v.getId() == R.id.btnCardId) onButtonCardId();
        else if (v.getId() == R.id.btnClear) onButtonClear();
        else if (v.getId() == R.id.btnPair) onButtonPair();
    }

    // for data input dialog
    private DialogInterface.OnClickListener mDialogDataInputListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == Dialog.BUTTON_POSITIVE) {
                updateInputData();
                execCommand(mCommand);
            }
        }
    };

    private DialogInterface.OnClickListener mDialogDeviceListListener;

    {
        mDialogDeviceListListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // connect to the device
                int index = which;
                // scan button
                if (index == Dialog.BUTTON_POSITIVE) onButtonScan();
                else if (index >= 0 && index < mListDeviceName.size()) {
                    // select a device from list, connect
                    String addr = mListDeviceAddress.get(index);
                    mScanMode = false;
                    mController.connect(addr);
                }
            }
        };
    }

    private void onButtonScan() {
        int err;

        if (null == mController) {
            log("Service is not ready!");
            return;
        }

        if (mController.isIdle()) {
            mScanMode = true;
            mListDeviceName.clear();
            mListDeviceAddress.clear();
            err = mController.scan();
            if (err != Data.ERROR_OK) log("start scan failed, err=" + errDesc(err));
        } else if (mScanMode) mController.stop();
    }

    private void onButtonStart() {
        AlertDialog.Builder b;
        String[] items;
        int index;
        if (null == mController) {
            log("Service is not ready!");
            return;
        }
        if (mScanMode) return;

        if (mListDeviceAddress.size() == 0) {
            log("No device, please scan device firstly");
            return;
        }
        if (!mController.isIdle()) mController.disconnect();
        else {
            // has devices and is idle, display device list
            b = new AlertDialog.Builder(this);
            b.setTitle("Device List");
            items = new String[mListDeviceName.size()];
            for (index = 0; index < mListDeviceName.size(); index++)
                items[index] = mListDeviceName.get(index) + " ("
                        + mListDeviceAddress.get(index) + ")";
            b.setItems(items, mDialogDeviceListListener);
            b.setPositiveButton(R.string.scan, mDialogDeviceListListener);
            b.setNegativeButton(R.string.cancel, mDialogDeviceListListener);
            b.show();
        }
    }

    private void onButtonPair() {
        byte[] key = "123456".getBytes();
        mCommand = APIId.PairDevice.ordinal();
        setDataLength(key.length);
        showDataUI(APIId.PairDevice, "Authenticate Key", key, true);
    }

    private void onButtonCardId() {
        int err;
        if (null == mController) {
            log("Service is not ready!");
            return;
        }
        if (!mController.isReady()) return;
        if (mController.isBusy()) return;
        log("checking Card Id...");
        mTestCardId = true;
        err = mController.antennaControl(true);
        if (Data.ERROR_OK != err) {
            mTestCardId = false;
            log("command failed! code=" + errDesc(err));
            log(STR_LINE_COMMAND);
        }
    }

    private void onButtonClear() {
        mAdapterLog.clear();
        mAdapterLog.notifyDataSetChanged();
    }

    private String errDesc(int err) {
        String desc;
        if (err == Data.ERROR_OK) desc = "ok";
        else if (err == Data.ERROR_DEVICE_DISCONNECTED) desc = "device disconnected";
        else if (err == Data.ERROR_USER_CANCEL) desc = "user cancel";
        else if (err == Data.ERROR_BUSY) desc = "ble is busy";
        else if (err == Data.ERROR_NO_DEVICE) desc = "no device";
        else if (err == Data.ERROR_NO_SERVICE) desc = "no service";
        else if (err == Data.ERROR_FAILED_TO_SET_CHARACTERISTIC)
            desc = "write characteristic error";
        else if (err == Data.ERROR_FAILED_TO_SET_DESCRIPTOR) desc = "write descriptor error";
        else if (err == Data.ERROR_NOT_READY) desc = "not ready";
        else if (err == Data.ERROR_NOT_SUPPORTED) desc = "not supported";
        else desc = "" + err;
        return desc;
    }

    private void updateInputData() {
        RadioButton rb;
        if (null != mInputData.labelBool) {
            rb = mDialogView.findViewById(R.id.radioTrue);
            mInputData.boolValue = rb.isChecked();
        }

        if (null != mInputData.labelByte1)
            mInputData.byteValue1 = Util.hex2byte(Objects.requireNonNull(getText(mDialogView,
                    R.id.edByte1)));

        if (null != mInputData.labelByte2)
            mInputData.byteValue2 = Util.hex2byte(Objects.requireNonNull(getText(mDialogView,
                    R.id.edByte2)));

        if (null != mInputData.labelData1) if (mInputData.asciiData1)
            mInputData.dataValue1 = Objects.requireNonNull(getText(mDialogView, R.id.edData1))
                    .getBytes();
        else mInputData.dataValue1 = Util.hex2bytes(getText(mDialogView,
                    R.id.edData1));
    }

    private void setText(View parent, int id, String text) {
        TextView v = parent.findViewById(id);
        if (null != v) v.setText(text);
    }

    private void goneView(View parent, int id) {
        View v = parent.findViewById(id);
        if (null != v) v.setVisibility(View.GONE);
    }

    private void HexMonitor(View parent, int id) {
        EditText ed = parent.findViewById(id);
    }

    private void setDataLength(int length) {
        mInputData.setDataLength(length);
    }

    @SuppressLint("InflateParams")
    private void showDataUI() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View view;
        EditText ed;
        RadioButton rb;
        int l;
        mDialogView = this.getLayoutInflater().inflate(R.layout.activity_data,
                null);

        view = mDialogView;
        b.setTitle(mInputData.title);
        b.setView(view);
        b.setPositiveButton(R.string.ok, mDialogDataInputListener);
        b.setNegativeButton(R.string.cancel, mDialogDataInputListener);

        if (null == mInputData.labelBool) goneView(view, R.id.layoutBool);
        else {
            setText(view, R.id.tvBoolLabel, mInputData.labelBool);
            rb = view.findViewById(R.id.radioTrue);
            rb.setChecked(mInputData.boolValue);
            rb = view.findViewById(R.id.radioFalse);
            rb.setChecked(!mInputData.boolValue);
        }

        if (null == mInputData.labelByte1) goneView(view, R.id.layoutByte1);
        else {
            HexMonitor(view, R.id.edByte1);
            setText(view, R.id.tvByte1, mInputData.labelByte1);
            setText(view, R.id.edByte1, Util.hexstr(mInputData.byteValue1));
        }

        if (null == mInputData.labelByte2) goneView(view, R.id.layoutByte2);
        else {
            HexMonitor(view, R.id.edByte2);
            setText(view, R.id.tvByte2, mInputData.labelByte2);
            setText(view, R.id.edByte2, Util.hexstr(mInputData.byteValue2));
        }

        if (null == mInputData.labelData1) goneView(view, R.id.layoutData1);
        else {
            HexMonitor(view, R.id.edData1);
            if (mInputData.asciiData1)
                setText(view, R.id.tvData1, mInputData.labelData1 + " / char");
            else setText(view, R.id.tvData1, mInputData.labelData1 + " / hex");
            if (null != mInputData.dataValue1)
                if (mInputData.asciiData1) setText(view, R.id.edData1, new String(
                        mInputData.dataValue1));
                else setText(view, R.id.edData1,
                        Util.hexstr(mInputData.dataValue1, false));
            ed = view.findViewById(R.id.edData1);
            if (null != ed) {
                InputFilter lengthFilter;
                TextWatcher textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        int l;
                        String text;
                        text = s.toString();
                        if (null == text) l = 0;
                        else l = text.length();
                        if (mInputData.asciiData1) setText(mDialogView, R.id.tvLengthIndicator, ""
                                + (mInputData.dataLength1 - l));
                        else setText(mDialogView, R.id.tvLengthIndicator, ""
                                + ((mInputData.dataLength1 * 2) - l));
                    }
                };
                ed.addTextChangedListener(textWatcher);
                InputFilter[] filters;
                filters = new InputFilter[1];
                // filters[1] = new InputFilter.AllCaps();
                if (mInputData.asciiData1) {
                    lengthFilter = new InputFilter.LengthFilter(
                            mInputData.dataLength1);
                    ed.setKeyListener(new MyNumberKeyListener(false));
                } else {
                    lengthFilter = new InputFilter.LengthFilter(
                            mInputData.dataLength1 * 2);
                    ed.setKeyListener(new MyNumberKeyListener(true));
                }
                filters[0] = lengthFilter;
                ed.setFilters(filters);
            }
            if (null == mInputData.dataValue1) l = 0;
            else l = mInputData.dataValue1.length;
            if (mInputData.asciiData1) setText(mDialogView, R.id.tvLengthIndicator, ""
                    + (mInputData.dataLength1 - l));
            else setText(mDialogView, R.id.tvLengthIndicator, ""
                    + (mInputData.dataLength1 - l) * 2);
        }
        b.show();
    }

    private void showDataUI(APIId api, String labelBool, boolean boolValue) {
        mInputData.set(api.toString(), labelBool, boolValue);
        showDataUI();
    }

    private void showDataUI(APIId api, String labelBool, boolean boolValue,
                            String labelByte1, byte byteValue1, String labelData1,
                            byte[] dataValue1) {
        mInputData.set(api.toString(), labelBool, boolValue, labelByte1,
                byteValue1, labelData1, dataValue1);
        showDataUI();
    }

    @SuppressWarnings("unused")
    private void showDataUI(APIId api, String labelByte1, byte byteValue1,
                            String labelData1, byte[] dataValue1) {
        mInputData.set(api.toString(), labelByte1, byteValue1, labelData1,
                dataValue1);
        showDataUI();
    }

    @SuppressWarnings("unused")
    private void showDataUI(APIId api, String labelByte1, byte byteValue1,
                            String labelByte2, byte byteValue2) {
        mInputData.set(api.toString(), labelByte1, byteValue1, labelByte2,
                byteValue2);
        showDataUI();
    }

    @SuppressWarnings("unused")
    private void showDataUI(APIId api, String labelByte1, byte byteValue1) {
        // showDataUI(api, labelByte1, byteValue1, null, (byte)0);
        mInputData.set(api.toString(), labelByte1, byteValue1);
        showDataUI();
    }

    private void showDataUI(APIId api, String dataLabel1, byte[] dataValue1,
                            boolean ascii) {
        mInputData.set(api.toString(), dataLabel1, dataValue1, ascii);
        showDataUI();
    }

    @SuppressWarnings("unused")
    private void showDataUI(APIId api, String dataLabel1, byte[] dataValue1) {
        showDataUI(api, dataLabel1, dataValue1, false);
    }

    private String getText(View parent, int id) {
        TextView tv;
        tv = parent.findViewById(id);
        if (null != tv) return tv.getText().toString();
        return null;
    }

    @Override
    public void onStateChange(int old_state, int new_state, int error) {
        String mDeviceAddress;
        if (new_state == Data.STATE_SCANNING) {
            log("scanning device...");
            setText(R.id.btnScan, R.string.stop);
            enableView(R.id.btnStart, false);
        } else if (new_state == Data.STATE_CONNECTING_DEVICE) {
            log("conn: " + mController.getDeviceName());
            log("addr: " + mController.getDeviceAddress());
            enableView(R.id.btnScan, false);
            setText(R.id.btnStart, R.string.disconnect);
        } else if (new_state == Data.STATE_END) {
            if (mScanMode) {
                log("scanning stop!");
                mScanMode = false;
                setText(R.id.btnScan, R.string.scan);
                if (mListDeviceAddress.size() > 0) {
                    enableView(R.id.btnStart, true);
                }
            } else {
                setTitle("Disconnected");
                log("disconnected! code=" + errDesc(error));
                setText(R.id.btnStart, R.string.connect);
                enableView(R.id.btnScan, true);
                enableView(R.id.btnPair, false);
                enableView(R.id.btnCardId, false);
            }
            log(STR_LINE_SESSION);
            log("");
        } else if (new_state == Data.STATE_READY) {
            log("connect ok!");
            log("");
            mDeviceAddress = mController.getDeviceAddress();
            setTitle("Connected: " + mDeviceAddress);
            enableView(R.id.btnPair, true);
            enableView(R.id.btnCardId, true);
        }
    }

    @Override
    public void onResult(byte[] data, int dataLength) {
        byte[] tmp = new byte[dataLength];
        System.arraycopy(data, 0, tmp, 0, dataLength);

        if (null != data) log("recv: " + Util.hexstr(tmp, true));
        else log("info: no data!");
    }

    @Override
    public void onWrite(byte[] data, int dataLength) {
        byte[] tmp = new byte[dataLength];
        System.arraycopy(data, 0, tmp, 0, dataLength);
        log("send: " + Util.hexstr(tmp, true));
    }

    @Override
    public void onFoundDevice(String deviceName, String deviceAddress) {
        if (!mListDeviceAddress.contains(deviceAddress)) {
            mListDeviceName.add(deviceName);
            mListDeviceAddress.add(deviceAddress);
            log("scan device: " + deviceName + ", " + deviceAddress);
        }
    }

    public void onResult(Result result) {
        try {
            if (null == result) {
                log("info: no data!");
                ReSend();
                return;
            } else if (result.getCommand() != Command.ExchangeTransparentData)
                log("info: " + result.getCommand() + ", "
                        + result.getStatus());

            if (result.getCommand() == Command.SelectCard
                    && result.getStatus() == Status.OperationSuccess)
                log("      Id: " + Util.hexstr(result.getCardID(), false)
                        + ", Type: 0x"
                        + Util.hexstr(result.getCardType().getValue()));

            if (result.getStatus() == Status.AuthenticationRequired) {
                log("Please pair the device firstly");
                mTestCardId = false;
            }

            if (null != result.getData()) {
                endtime = System.currentTimeMillis();
                costTime = (endtime - begintime);
                try {
                    if (!ErrMessage(Util.hexstr(result.getData(), false)).equals("")
                            | ErrMessage(Util.hexstr(result.getData(), false))
                            .equals("")) {
                        log("返回<<== " + Util.hexstr(result.getData(), true));
                        switch (flag) {
                            case 1:
                                masterkey = Util.hexstr(result.getData(), false)
                                        .substring(4, 132);
                                log("双重加密后的SEED：  " + AddSpace(masterkey));
                                break;
                            case 2:
                                PublicKey = Util.hexstr(result.getData(), false)
                                        .substring(4, 132);
                                log("公钥数据-比特币：  " + AddSpace(PublicKey));
                                break;
                            case 3:
                                SignValue = Util.hexstr(result.getData(), false)
                                        .substring(4, 132);
                                log("签名结果-比特币：  " + AddSpace(SignValue));
                                break;
                            case 4:
                                PublicKey = Util.hexstr(result.getData(), false)
                                        .substring(4, 132);
                                log("公钥数据-以太坊：  " + AddSpace(PublicKey));
                                break;
                            case 5:
                                SignValue = Util.hexstr(result.getData(), false)
                                        .substring(4, 132);
                                log("签名结果-以太坊：  " + AddSpace(SignValue));
                                break;
                            case 6:
                                log("成功标识：  "
                                        + Util.hexstr(result.getData(), false)
                                        .substring(4, 6)
                                        + "     [00成功/01失败]");
                                break;
                        }
                        log("用时：  " + costTime);
                    } else log("返回<<== " + errmessage);

                } catch (Exception e) {
                    log(e.toString());
                    ReSend();
                }
            }
        } catch (Exception e) {
            log(e.toString());
            ReSend();
        }

        log(STR_LINE_COMMAND);
    }

    /**
     * 重发APDU指令
     */

    public void ReSend() {
        final byte[] Apdudata;
        Apdudata = Util.hexStringToByte(StrApdu.toUpperCase()
                .replace(" ", ""));
        mController.exchangeTransparentData(Apdudata, Apdudata.length);
    }

    public String ErrMessage(String Sw) {
        errmessage = "";
        if (Sw.equals("6E 00")) errmessage = "CLA不合法";
        if (Sw.equals("6D 00")) errmessage = "INS不合法";
        if (Sw.equals("6A 86")) errmessage = "p1，p2参数不合法";
        if (Sw.equals("67 00")) errmessage = "Lc长度不正确";
        if (Sw.equals("69 85")) errmessage = "卡片SEED已存在";
        if (Sw.equals("69 88")) errmessage = "私钥不存在";
        return errmessage;
    }
}
