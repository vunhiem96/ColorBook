package com.volio.coloringbook2.fragment


import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import com.tmall.ultraviewpager.UltraViewPager
import com.tmall.ultraviewpager.transformer.UltraDepthScaleTransformer
import com.volio.coloringbook2.R
import com.volio.coloringbook2.common.AppConst
import com.volio.coloringbook2.common.AppConst.TEMP_FOLDER
import com.volio.coloringbook2.common.gg
import com.volio.coloringbook2.common.gone
import com.volio.coloringbook2.common.setImageTint
import com.volio.coloringbook2.customview.ColourImageView
import com.volio.coloringbook2.customview.photoview.PhotoViewAttacher
import com.volio.coloringbook2.database.config
import com.volio.coloringbook2.interfaces.ColorInterfaces
import com.volio.coloringbook2.interfaces.ImageInterface
import com.volio.coloringbook2.java.*
import com.volio.coloringbook2.java.util.OnCustomClickListener
import com.volio.coloringbook2.model.storybook.StoryBook
import com.volio.coloringbook2.models.UltraPagerAdapter
import com.zxy.tiny.Tiny
import kotlinx.android.synthetic.main.fragment_edit.*
import java.io.File


private const val ARG_PARAM1 = "url"
private const val ARG_PARAM2 = "isFromMain"
private const val ARG_PARAM3 = "isRestart"

class EditFragment : BaseFragment(), OnCustomClickListener, SaveInterface, ImageInterface, ColorInterfaces {
    private var edit = -1
    private var idList = -1
    private var idImage = -1
    private var imageUrl: String? = null
    private var isFromMain = false
    private var imageName: String? = null
    private var isRestart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            imageUrl = it.getString(ARG_PARAM1)
            isFromMain = it.getBoolean(ARG_PARAM2)
            isRestart = it.getBoolean(ARG_PARAM3)
            idList = it.getInt("idList",-1)
            idImage = it.getInt("idImg",-1)
            edit= it.getInt("edit",-1)
        }
        if (idList != -1) {

            val apiJson = context!!.config.storyBook
            val gson = Gson()
            val listStoryBook = gson.fromJson(apiJson, StoryBook::class.java)
            val urlBase = "http://mycat.asia/volio_colorbook/"
            val url  = listStoryBook[idList].list[idImage].image_url
            imageUrl = "$urlBase$url"
        }
        setBackPress()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isNeedInit = false
        if (view1 == null) {
            isNeedInit = true
            view1 = inflater.inflate(R.layout.fragment_edit, container, false)
        }
        return view1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        Glide.with(context!!).asBitmap().load(imageUrl).diskCacheStrategy(DiskCacheStrategy.NONE).into(imgPreView)
        if (AppConst.isRestartImage) {
            AppConst.isRestartImage = false
            restart()
        }
        if (!isNeedInit) return
        initEvent()
        initView()
        PhotorTool.createFolder(TEMP_FOLDER)
        initViewPager()
        imageView?.onRedoUndoListener = object : ColourImageView.OnRedoUndoListener {
            override fun onRedoUndo(undoSize: Int, redoSize: Int) {
                if (undoSize != 0) {
                    btn_back_image.alpha = 1F
                    btn_back_image.isEnabled = true
                } else {
                    btn_back_image.alpha = 0.5F
                    btn_back_image.isEnabled = false
                }
                if (redoSize != 0) {
                    btn_forward_image.alpha = 1F
                    btn_forward_image.isEnabled = true
                } else {
                    btn_forward_image.alpha = 0.5F
                    btn_forward_image.isEnabled = false
                }
            }
        }
    }


    private var adapter: PagerAdapter? = null
    private fun initViewPager() {
        ultraViewPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL)
        adapter = UltraPagerAdapter(true, AppConst.listAllColor, this)
        ultraViewPager.adapter = adapter
        ultraViewPager.setMultiScreen(0.75f)
        ultraViewPager.setAutoMeasureHeight(false)
        ultraViewPager.setPageTransformer(false, UltraDepthScaleTransformer())
        ultraViewPager.setInfiniteLoop(true)
    }

    override fun pickColor(color: String) {

        imageView?.setColor(Color.parseColor(color))
        img_current_color.setImageTint(Color.parseColor(color))
    }

    override fun onClickBack() {
        super.onClickBack()
        Lo.d("onClickBack")
        PhotorDialog.getInstance().showDialogConfirm(activity, R.string.end_edit, R.string.no, R.string.yes, true,
            { dialog, _ ->
                //click no
                dialog.dismiss()
            }, { dialog, _ ->
                //click yes
                dialog.dismiss()
                saveCurrentImage()
            }
        )
//        Navigation.findNavController(view!!).popBackStack()
    }

    private var isSaveImaged = false
    private fun saveImage() {
//        showDialog(R.string.please_wait_to_save)
        if (imageView != null) {
            val bitmap = imageView?.getBitmap() ?: return
            ViewToBitmap.of(bitmap).setOnSaveResultListener { isSaved, path ->
                if (isSaved && path != null && path.isNotEmpty()) {
                    isSaveImaged = true
                    if (isSafe()) {
                        nextFragment(path)
                    }
                }
            }.toJPG().saveBitmap(context!!)
        } else {
            Toast.makeText(context, getString(R.string.st_went_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    private fun nextFragment(path: String) {
        val bundle = Bundle()
        bundle.putString("param1", path)
        findNavController().navigate(R.id.action_editFragment_to_saveFragment, bundle)
    }


    private fun initView() {
        initImage()
    }

    private var imageUrl2 = ""

    private fun initImage() {
        if (imageUrl != null && imageUrl!!.isNotEmpty()) {
            if (isFromMain) {
                imageName = imageUrl
                if (PhotorTool.checkHasPermission(activity!!)) {
                    val path = TEMP_FOLDER
                    val directory = File(path)
                    val files = directory.listFiles()
                    if (files == null) {
                        loadImage()
                        return
                    }
                    if (directory.isDirectory) {
                        if (files.isEmpty()) {
                            loadImage()
                        } else {
                            for (file in files) {
                                val url = file.absolutePath
                                var s = url
                                s = s?.substring(s.lastIndexOf("/") + 1)
                                s = s?.substring(0, s.lastIndexOf("."))
                                if (imageUrl.equals(s)) {
                                    imageUrl2 = url
                                    Glide.with(context!!).load(url).diskCacheStrategy(DiskCacheStrategy.NONE).into(imgPreView)
                                    showPopupCreateNew()
                                    return
                                }
                            }
                            loadImage()
                        }
                    }
                } else {
                    loadImage()
                }


            } else {
                var s = imageUrl
                s = s?.substring(s.lastIndexOf("/") + 1)
                s = s?.substring(0, s.lastIndexOf("."))
                imageName = s

                Lo.d("imageurl $imageUrl")
                Lo.d("imageName $imageName")

                loadImage()
            }

        } else {
            //todo somethin wrong
        }
    }

    private fun showPopupCreateNew() {
        PhotorDialog.getInstance().showDialogConfirm(activity, R.string.continue_edit_or_new,
            R.string.continuee, R.string.rework, false, { _, _ ->
                //continue
                imageUrl = imageUrl2
                isFromMain = false
                loadImage()
            }, { _, _ ->
                //rework
                loadImage()
            })


    }

    private fun loadImage() {
        if (isRestart) isFromMain = true

        val id = AppConst.getIdRawFromName(context!!, imageName!!)
//        if (id == 0) {
//            To.show(R.string.try_again)
//            return
//        }

        val compressOptions = Tiny.BitmapCompressOptions()
        compressOptions.config = Bitmap.Config.ARGB_8888
        if (isFromMain) {
            Tiny.getInstance().source(id!!).asBitmap().withOptions(compressOptions).compress { isSuccess, bitmap, t ->
                if (isSuccess) {
                    try {
                        imgPreView?.visibility = View.GONE
                        imageView?.createBitMap(bitmap!!)
                        PhotoViewAttacher(imageView, bitmap)
                    } catch (e: IllegalStateException) {
                    }
                } else {
                    loadImageOld(id)
                }
            }
        } else {
            Tiny.getInstance().source(imageUrl).asBitmap().withOptions(compressOptions).compress { isSuccess, bitmap, t ->
                if (isSuccess) {
                    try {
                        imgPreView?.visibility = View.GONE
                        imageView?.createBitMap(bitmap!!)
                        PhotoViewAttacher(imageView, bitmap)
                    } catch (e: IllegalStateException) {
                    }
                } else {
                    loadImageOld(id!!)
                }
            }
        }


    }

    private fun loadImageOld(id: Int) {
        var bitmap: Bitmap? = null
        PhotorThread.getInstance().runBackground(object : PhotorThread.IBackground {
            override fun doingBackground() {
                bitmap = Glide.with(context!!)
                    .asBitmap()
                    .load(if (isFromMain) {
//                        id
                        imageUrl
                    } else {
                        imageUrl
                    })
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .submit().get()
            }

            override fun onCompleted() {
                try {
                    imgPreView?.visibility = View.GONE
                    imageView?.createBitMap(bitmap!!)
                    PhotoViewAttacher(imageView, bitmap)
                } catch (e: IllegalStateException) {
                }
            }

            override fun onCancel() {
            }

        })
    }


    override fun restart() {
        val compressOptions = Tiny.BitmapCompressOptions()
        compressOptions.config = Bitmap.Config.ARGB_8888
        Tiny.getInstance().source(AppConst.getIdRawFromName(context!!, imageName!!)!!).asBitmap().withOptions(compressOptions).compress { isSuccess, bitmap, t ->
            if (isSuccess) {
                imageView?.createBitMap(bitmap!!)
            } else {
                loadBitmapRestart()
            }
        }
    }

    private fun loadBitmapRestart() {
        var bitmap: Bitmap? = null
        PhotorThread.getInstance().runBackground(object : PhotorThread.IBackground {
            override fun doingBackground() {
                bitmap = Glide.with(context!!)
                    .asBitmap()
                    .load(AppConst.getIdRawFromName(context!!, imageName!!))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .submit().get()
            }

            override fun onCompleted() {
                imageView?.createBitMap(bitmap!!)
            }

            override fun onCancel() {
            }

        })
    }


    private fun changeCurrentColor(color: Int) {
        imageView?.setColor(color)
    }


    private fun initEvent() {
        PhotorTool.clickScaleView(btn_back_main, this)
        PhotorTool.clickScaleView(btn_idea_main, this)
        PhotorTool.clickScaleView(btn_back_image, this)
        PhotorTool.clickScaleView(btn_forward_image, this)
        btn_share.setOnClickListener {
            if (edit == 1001){
                val file = File(imageUrl)
                file.delete()
                context!!.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(imageUrl))))

            }
            Log.e("MEOMEO", "click save")
            if (!canTouch()) return@setOnClickListener
            if (PhotorTool.checkHasPermission(activity!!)) {
                saveImage()
            } else {
                askPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    if (it.isAccepted) {
                        saveImage()
                    }
                }
            }
        }

    }

    override fun setTextColor(color: Int) {
        changeCurrentColor(color)
    }

    private fun saveCurrentImage() {
        if (activity != null) {
            if (PhotorTool.checkHasPermission(activity!!)) {
                cacheImage()
            } else {
                findNavController().popBackStack(R.id.menuFragment, false)
//                findNavController().navigate(R.id.action_editFragment_to_menuFragment)
            }
        }
    }

    private fun cacheImage() {
        val imageUrl1: String = if (isFromMain) {
            "$TEMP_FOLDER$imageUrl.jpg"
        } else {
            "$imageUrl"
        }
        val bitmap = imageView?.getBitmap()
        if (bitmap != null) {
            AppConst.saveBitmap(imageUrl1, "", bitmap)
        }
        findNavController().popBackStack(R.id.menuFragment, false)
//        findNavController().navigate(R.id.action_editFragment_to_menuFragment)
//        mChangeFragmentListener?.popToRootFragments()
    }


    private var current_click_idea = 0
    private fun clickIdea() {
        if (current_click_idea < 3) {
            current_click_idea++
            when (current_click_idea) {
                1 -> Glide.with(context!!).load(R.drawable.bt_idea_2).into(btn_idea_main)
                2 -> Glide.with(context!!).load(R.drawable.bt_idea_1).into(btn_idea_main)
                3 -> {
                    Glide.with(context!!).load(R.drawable.bt_idea_ads).into(btn_idea_main)
                    btn_idea_main.isEnabled = true
//                    btn_idea_main.alpha = 0.5F
                }
            }
            val builder: AlertDialog.Builder?
            val inflater = this.layoutInflater
            builder = AlertDialog.Builder(context!!)
            val logoutDialog = inflater.inflate(R.layout.dialog_idea, null)
            val btnCancel: ImageView = logoutDialog.findViewById(R.id.btn_close_dialog)
            val imgIdea: ImageView = logoutDialog.findViewById(R.id.img_idea)
            if (isFromMain) {
                Glide.with(context!!).load(AppConst.getIdRawFromName(context!!, imageUrl + "_mau")).into(imgIdea)
            } else {
                var s = imageUrl
                s = s?.substring(s.lastIndexOf("/") + 1)
                s = s?.substring(0, s.lastIndexOf("."))
                Glide.with(context!!).load(AppConst.getIdRawFromName(context!!, s + "_mau")).into(imgIdea)
            }

            builder.setView(logoutDialog)
            builder.setCancelable(true)
            val dialog: AlertDialog = builder.create()
            dialog.show()
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        } else {
        }
    }

    override fun eventStateBack(able: Boolean) {
        Lo.d(able.toString())
        if (able) {
            btn_back_image.alpha = 1F
            btn_back_image.isEnabled = true
        } else {
            btn_back_image.isEnabled = false
            btn_back_image.alpha = 0.5F
        }
    }

    override fun eventStateForward(able: Boolean) {
        if (able) {
            btn_forward_image.alpha = 1F
            btn_forward_image.isEnabled = true
        } else {
            btn_forward_image.alpha = 0.5F
            btn_forward_image.isEnabled = false
        }
    }

    override fun backInMain() {
        super.backInMain()

        PhotorDialog.getInstance().showDialogConfirm(activity, R.string.end_edit, R.string.no, R.string.yes, true,
            { dialog, _ ->
                //click no
                dialog.dismiss()
//                isSaveImage = false
//                mChangeFragmentListener?.popBackStackFragment("MenuFragment")
            }, { dialog, _ ->
                //click yes
                dialog.dismiss()
                saveCurrentImage()
            }
        )
    }


    override fun loadBitmapSuccess() {
        progressBar.gone()
    }

    override fun OnCustomClick(v: View?, event: MotionEvent?) {
        if (!canTouch()) return
        when (v?.id) {
            R.id.btn_back_main -> backInMain()
            R.id.btn_idea_main -> clickIdea()
            R.id.btn_back_image -> {
                imageView?.actionBackStep()
            }
            R.id.btn_forward_image -> {
                imageView?.actionForwardStep()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (context != null) {
            Glide.get(context!!).clearMemory()
        }
        PhotorThread.getInstance().removeAllBackgroundThreads()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        PhotorDialog.getInstance().showDialogConfirm(activity, R.string.end_edit, R.string.no, R.string.yes, true,
            { dialog, _ ->
                //click no
                dialog.dismiss()
//                isSaveImage = false
//                mChangeFragmentListener?.popToRootFragments()
            }, { dialog, _ ->
                dialog.dismiss()
                if (!isSaveImaged) {
                    saveCurrentImage()
                } else {
                    mChangeFragmentListener?.popToRootFragments()
                }
            }
        )
    }
}
