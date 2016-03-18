package com.example.janiszhang.bitmaptest;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {

    private static final int RESULT = 0;
    private Button mLoadButton;
    private Button mSaveButton;
    private ImageView mIv;
    private Bitmap mBitmap;
    private Bitmap mAlterBitmap;
    private Canvas mCanvas;
    private Paint mPaint;

    private float downx = 0;
    private float downy = 0;
    private float upx = 0;
    private float upy = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadButton = (Button) findViewById(R.id.btn_load);
        mSaveButton = (Button) findViewById(R.id.btn_save);
        mIv = (ImageView) findViewById(R.id.iv);

        mLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//调用android图库
                startActivityForResult(intent, RESULT);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存画好的图片
                if(mAlterBitmap!=null){
                    try {
                        Uri imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                        OutputStream outputStream=getContentResolver().openOutputStream(imageUri);
                        mAlterBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
                        Toast.makeText(getApplicationContext(), "save!", Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode ==RESULT_OK) {
            try {

                Uri imageFileUri = data.getData();
                Display defaultDisplay = getWindowManager().getDefaultDisplay();
                float ddw = defaultDisplay.getWidth();
                float ddh = defaultDisplay.getHeight();

//            BitmapFactory.Options options = new BitmapFactory().Options();//注意别写错了,options是静态内部类,需要直接使用外部类直接饮用
                BitmapFactory.Options options = new BitmapFactory.Options();
                /**
                 * inJustDecodeBounds 如果设置为true，并不会把图像的数据完全解码，亦即decodeXyz()返回值为null，但是Options的outAbc中解出了图像的基本信息。
                 * 先设置inJustDecodeBounds= true，调用decodeFile()得到图像的基本信息,利用图像的宽度（或者高度，或综合）以及目标的宽度，得到inSampleSize值，
                 * 再设置inJustDecodeBounds= false，调用decodeFile()得到完整的图像数据。
                 * 先获取比例，再读入数据，如果欲读入大比例缩小的图，将显著的节约内容资源。有时候还会读入大量的缩略图，这效果就更明显了。
                 */
                options.inJustDecodeBounds = true;
                mBitmap = BitmapFactory.decodeStream(getContentResolver()
                        .openInputStream(imageFileUri), null, options);//需要权限android.permission.READ_EXTERNAL_STORAGE

                //计算缩放因子
                int heightRatio = (int) Math.ceil(options.outHeight/ddh);
                int widthRatio = (int) Math.ceil(options.outWidth/ddw);
                if (heightRatio > widthRatio) {
                    options.inSampleSize = heightRatio;
                } else {
                    options.inSampleSize = widthRatio;
                }

                options.inJustDecodeBounds = false;
                mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileUri), null, options);
                mAlterBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), mBitmap.getConfig());

                mCanvas = new Canvas(mAlterBitmap);
                mPaint = new Paint();
                mPaint.setColor(Color.GREEN);
                mPaint.setStrokeWidth(5);
                Matrix matrix = new Matrix();//矩阵

                //这里的用法看不懂,mbitmap和mAlterBitmap???
                mCanvas.drawBitmap(mBitmap, matrix, mPaint);
                mIv.setImageBitmap(mAlterBitmap);

                mIv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        switch (action) {
                            case MotionEvent.ACTION_DOWN:
                                downx = event.getX();
                                downy = event.getY();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                // 路径画板
                                upx = event.getX();
                                upy = event.getY();
                                mCanvas.drawLine(downx, downy, upx, upy, mPaint);
                                mIv.invalidate();
                                downx = upx;
                                downy = upy;
                                break;
                            case MotionEvent.ACTION_UP:
                                // 直线画板
                                break;

                            default:
                                break;
                        }

                        return true;
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
