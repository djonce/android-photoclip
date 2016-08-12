package tech.wangj.library;


import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * 图片剪切类
 *
 * @author rendongwei
 */
public class PhotoClipActivity extends Activity {
    private CropImageView mDisplay;
    private ProgressBar mProgressBar;
    private ImageView mLeft;
    private ImageView mRight;
    private ImageView btnBack;
    private ImageView confirm;

    public static final int SHOW_PROGRESS = 0;
    public static final int REMOVE_PROGRESS = 1;

    private String mPath;// 修改的图片地址
    private Bitmap mBitmap;// 修改的图片
    private CropImage mCropImage; // 裁剪工具类

    public static String CROP_TYPE = "image_crop_type";
    public static int CORP_KEY_AVATAR = 0; // 方形 图像剪裁
    public static int CORP_KEY_COVER = 1;  // 长方形 16/9 视频封面剪裁

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_clip);
        findViewById();
        setListener();
        init();
    }

    private void findViewById() {
        btnBack = (ImageView) findViewById(R.id.btnBack);
        confirm = (ImageView) findViewById(R.id.confirm);
        mDisplay = (CropImageView) findViewById(R.id.imagefilter_crop_display);
        mProgressBar = (ProgressBar) findViewById(R.id.imagefilter_crop_progressbar);
        mLeft = (ImageView) findViewById(R.id.btn_left);
        mRight = (ImageView) findViewById(R.id.btn_right);
    }

    private void setListener() {
        btnBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // 显示返回对话框
                backDialog();
            }
        });
        confirm.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // 保存修改的图片到本地,并返回图片地址  TODO HM NOTE 1S CT 拍照会报 java.io.IOException: open failed: EACCES (Permission denied) 但是权限都已经加过了，未知原因
                mPath = PhotoUtil.saveToLocal(mCropImage.cropAndSave());
                Intent intent = new Intent();
                intent.putExtra("path", mPath);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mLeft.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // 左旋转
                mCropImage.startRotate(270.f);
            }
        });
        mRight.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // 有旋转
                mCropImage.startRotate(90.f);
            }
        });
    }

    private CropImage.CropType mCropType;

    public void setCropType(CropImage.CropType mCropType) {
        this.mCropType = mCropType;
    }

    private void init() {
        // 接收传递的图片地址
        mPath = getIntent().getStringExtra("path");

        int corpType = getIntent().getIntExtra(CROP_TYPE, CORP_KEY_AVATAR);

        if (corpType == CORP_KEY_AVATAR) {
            setCropType(CropImage.CropType.AVATAR);
        }else if (corpType == CORP_KEY_COVER) {
            setCropType(CropImage.CropType.COVER);
        }

        try {
            DisplayMetrics metric = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metric);
            // 获取修改的图片
            mBitmap = PhotoUtil.createBitmap(mPath, metric.widthPixels, metric.heightPixels);
            // 如果图片不存在,则返回,存在则初始化
            if (mBitmap == null) {
                Toast.makeText(PhotoClipActivity.this, R.string.nophoto, Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
            } else {
                resetImageView(mBitmap);
            }
        } catch (Exception e) {
            Toast.makeText(PhotoClipActivity.this, R.string.nophoto, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    /**
     * 初始化图片显示
     *
     * @param b
     */
    private void resetImageView(Bitmap b) {
        mDisplay.clear();
        mDisplay.setImageBitmap(b);
        mDisplay.setImageBitmapResetBase(b, true);
        mCropImage = new CropImage(this, mDisplay, handler);
        if (mCropType != null) {
            mCropImage.setCropType(mCropType);
        }
        mCropImage.crop(b);
    }

    /**
     * 控制进度条
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS:
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case REMOVE_PROGRESS:
                    handler.removeMessages(SHOW_PROGRESS);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    /**
     * 返回对话框
     */
    private void backDialog() {
        Builder builder = new Builder(PhotoClipActivity.this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage(R.string.confirmcanceleditphoto);
        builder.setPositiveButton(R.string.comfirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public void onBackPressed() {
        backDialog();
    }
}

