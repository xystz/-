package ifly.tts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.TextUnderstander;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;

import ifly.tts.speech.setting.TtsSettings;
import ifly.tts.speech.setting.UnderstanderSettings;
import ifly.tts.speech.util.ApkInstaller;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;

import com.iflytek.sunflower.FlowerCollector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import ifly.tts.speech.util.JsonParser;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends Activity implements View.OnClickListener {

    private static String TAG = MainActivity.class.getSimpleName();
    // 语义理解对象（语音到语义）。
    private SpeechUnderstander mSpeechUnderstander;
    // 语义理解对象（文本到语义）。
    private TextUnderstander mTextUnderstander;
    private Toast mToast;
    private EditText mUnderstanderText;

    private SharedPreferences mSharedPreferences;

    // 语音合成对象
    private SpeechSynthesizer mTts;

    // 默认发音人
    private String voicer = "xiaoyan";

    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue ;

    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;

    // 云端/本地单选按钮
    private RadioGroup mRadioGroup;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    // 语记安装助手类
    ApkInstaller mInstaller ;

    private ImageView voice_ = null;
    private byte flag = 0;

    //private voice voiceThread;
    /*
    private class voice extends Thread{

        @Override

        public void run() {
            super.run();
            while(true) {
                try {
                    if ((!mSpeechUnderstander.isUnderstanding()) && (flag == 0)) {
                        //    mSpeechUnderstander.stopUnderstanding();
                        //    showTip("停止录音");
                        Thread.currentThread().sleep(1000);
                        ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
                    }
                }catch (InterruptedException e){};
            }

        }

    }
    */
    private class Voice_ implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            MainActivity.this.voice_.setImageResource(R.drawable.voice_rh_100px);

            if(mSpeechUnderstander.isUnderstanding()){// 开始前检查状态
                mSpeechUnderstander.stopUnderstanding();
                showTip("停止录音");
            }else {
                ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
                if(ret != 0){
                    showTip("语义理解失败,错误码:"	+ ret);
                }
            }

        }
    }
    @SuppressLint("ShowToast")
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//省去标题栏
        setContentView(R.layout.activity_main);

        initLayout();
        this.voice_ = (ImageView) super.findViewById(R.id.voice);
        /**
         * 申请的appid时，我们为开发者开通了开放语义（语义理解）
         * 由于语义理解的场景繁多，需开发自己去开放语义平台：http://www.xfyun.cn/services/osp
         * 配置相应的语音场景，才能使用语义理解，否则文本理解将不能使用，语义理解将返回听写结果。
         */

        // 初始化对象
        mSpeechUnderstander = SpeechUnderstander.createUnderstander(MainActivity.this, mSpeechUdrInitListener);
        mTextUnderstander = TextUnderstander.createTextUnderstander(MainActivity.this, mTextUdrInitListener);
        mToast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);

        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this, mTtsInitListener);
        // 云端发音人名称列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
        mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE);
        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        mInstaller = new ApkInstaller(MainActivity.this);
        setParam();

//        voiceThread = new voice();
//        voiceThread.start();

    //    timer.schedule(task, 0, 2500);
        this.voice_.setOnClickListener(new Voice_());
    }

/**/
    private Timer timer = new Timer();
    private TimerTask task = new TimerTask() {
        public void run() {
            if((!mSpeechUnderstander.isUnderstanding()) && (flag == 0)) {
            //    mSpeechUnderstander.stopUnderstanding();
            //    showTip("停止录音");
                ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
            }
        }
    };

    /**
     * 初始化Layout。
     */
    private void initLayout(){
        findViewById(R.id.text_understander).setOnClickListener(MainActivity.this);
        findViewById(R.id.start_understander).setOnClickListener(MainActivity.this);

    //    mUnderstanderText = (EditText)findViewById(R.id.understander_text);

    //    findViewById(R.id.understander_stop).setOnClickListener(MainActivity.this);
     //   findViewById(R.id.understander_cancel).setOnClickListener(MainActivity.this);
//        findViewById(R.id.image_understander_set).setOnClickListener(MainActivity.this);

        mSharedPreferences = getSharedPreferences(UnderstanderSettings.PREFER_NAME, Activity.MODE_PRIVATE);
    }

    /**
     * 初始化监听器（语音到语义）。
     */
    private InitListener mSpeechUdrInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "speechUnderstanderListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："+code);
            }
        }
    };

    /**
     * 初始化监听器（文本到语义）。
     */
    private InitListener mTextUdrInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "textUnderstanderListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："+code);
            }
        }
    };


    int ret = 0;// 函数调用返回值
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 进入参数设置页面
            /*
            case R.id.image_understander_set:
                Intent intent = new Intent(MainActivity.this, UnderstanderSettings.class);
                startActivity(intent);
                break;
             */
            // 开始文本理解
            case R.id.text_understander:
                mUnderstanderText.setText("");
                String text = "合肥明天的天气怎么样？";
                showTip(text);

                if(mTextUnderstander.isUnderstanding()){
                    mTextUnderstander.cancel();
                    showTip("取消");
                }else {
                    ret = mTextUnderstander.understandText(text, mTextUnderstanderListener);
                    if(ret != 0)
                    {
                        showTip("语义理解失败,错误码:"+ ret);
                    }
                }
                break;
            // 开始语音理解
            case R.id.start_understander:
                mUnderstanderText.setText("");
                // 设置参数
                setParam();

                if(mSpeechUnderstander.isUnderstanding()){// 开始前检查状态
                    mSpeechUnderstander.stopUnderstanding();
                    //showTip("停止录音");
                }else {
                    ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
                    if(ret != 0){
                        showTip("语义理解失败,错误码:"	+ ret);
                    }else {
                        showTip(getString(R.string.text_begin));
                    }
                }
                break;
            /*
            // 停止语音理解
            case R.id.understander_stop:
                mSpeechUnderstander.stopUnderstanding();
                showTip("停止语义理解");
                break;
            // 取消语音理解
            case R.id.understander_cancel:
                mSpeechUnderstander.cancel();
                showTip("取消语义理解");
                break;
                */
            default:
                break;
        }
    }

    private TextUnderstanderListener mTextUnderstanderListener = new TextUnderstanderListener() {

        @Override
        public void onResult(final UnderstanderResult result) {
            if (null != result) {
                // 显示
                String text = result.getResultString();
                if (!TextUtils.isEmpty(text)) {
                    mUnderstanderText.setText(text);
                }
            } else {
                Log.d(TAG, "understander result:null");
                //showTip("识别结果不正确。");
            }
        }

        @Override
        public void onError(SpeechError error) {
            // 文本语义不能使用回调错误码14002，请确认您下载sdk时是否勾选语义场景和私有语义的发布
            showTip("onError Code："	+ error.getErrorCode());

        }
    };

    /**
     * 语义理解回d调。
     */
    private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {

        String cmd = null;
        @Override
        public void onResult(final UnderstanderResult result) {
            if (null != result) {
                Log.d(TAG, result.getResultString());

            //    String text = JsonParser.parseIatResult(result.getResultString());

                String sn = null;
                String su3 =null;
                // 读取json结果中的sn字段
                try {
                    JSONObject resultJson = new JSONObject(result.getResultString());
                    cmd = resultJson.optString("text");
                    sn = resultJson.optString("answer");
                    JSONObject su2 = new JSONObject(sn);
                    su3 = su2.optString("text");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // 显示k
                String text = result.getResultString();

                if (cmd.contains("不开心") || cmd.contains("难过") || cmd.contains("不高兴") || cmd.contains("无精打采")) {
                    GifImageView gib = (GifImageView) MainActivity.this.findViewById(R.id.view);
                    gib.setImageResource(R.drawable.wjdc);
                }
                //    String text = "123456";
                startspeech(su3);
                if (!TextUtils.isEmpty(text)) {
                //    mUnderstanderText.setText(text);
                }
            } else {
                //showTip("识别结果不正确。");
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            //showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, data.length+"");
        }

        @Override
        public void onEndOfSpeech() {
            MainActivity.this.voice_.setImageResource(R.drawable.voice_blue_100pix);
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
        //    showTip("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时释放连接
        mSpeechUnderstander.cancel();
        mSpeechUnderstander.destroy();
        if(mTextUnderstander.isUnderstanding())
            mTextUnderstander.cancel();
        mTextUnderstander.destroy();
    }

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    /**
     * 参数设置
     * @paramparam
     * @return
     */
    public void setParam(){
        String lang = mSharedPreferences.getString("understander_language_preference", "mandarin");
        if (lang.equals("en_us")) {
            // 设置语言
            mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "en_us");
        }else {
            // 设置语言
            mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mSpeechUnderstander.setParameter(SpeechConstant.ACCENT, lang);
        }
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mSpeechUnderstander.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("understander_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mSpeechUnderstander.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("understander_vadeos_preference", "1000"));

        // 设置标点符号，默认：1（有标点）
        mSpeechUnderstander.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("understander_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mSpeechUnderstander.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mSpeechUnderstander.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/sud.wav");
    }

    @Override
    protected void onResume() {
        //移动数据统计分析
        FlowerCollector.onResume(MainActivity.this);
        FlowerCollector.onPageStart(TAG);
        super.onResume();
    }

    @Override
    protected void onPause() {
        //移动数据统计分析
        FlowerCollector.onPageEnd(TAG);
        FlowerCollector.onPause(MainActivity.this);
        super.onPause();
    }




    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    public void startspeech(String text){
        // 移动数据分析，收集开始合成事件
        FlowerCollector.onEvent(MainActivity.this, "tts_play");

        //	String text = ((EditText) findViewById(R.id.tts_text)).getText().toString();

        // 设置参数
//        setParam();
        int code = mTts.startSpeaking(text, mTtsListener);
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
                //未安装则跳转到提示安装页面
                mInstaller.install();
            }else {
                showTip("语音合成失败,错误码: " + code);
            }
        }
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            //showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            //showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
            //showTip(String.format(getString(R.string.tts_toast_format),
            //        mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
            flag = 1;
            //showTip(String.format(getString(R.string.tts_toast_format),
            //        mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                flag = 0;
                showTip("播放完成");
            } else if (error != null) {
                flag = 0;
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

}
