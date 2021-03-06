package com.volio.coloringbook2.common

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import com.google.gson.Gson
import com.volio.coloringbook2.R
import com.volio.coloringbook2.database.config
import com.volio.coloringbook2.model.ColorBook
import com.volio.coloringbook2.models.ImageModel
import com.volio.coloringbook2.models.ListColorModel
import com.volio.coloringbook2.models.TypeModel
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

object AppConst {

    var isRestartImage = false

    var FOLDER_TEXT_TO_PHOTO =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/ColorToImage/"

    var FOLDER_STORY =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/StoryBook/"

    var FOLDER_SAVE =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/ColorToImage/vinh/"

    var TEMP_FOLDER =
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/ColorToImage/.temp/"

    const val EMAIL_FEEDBACK = "dreamgiant999@gmail.com"

    fun getAllType(context: Context): Array<TypeModel> {
        return arrayOf(
            TypeModel(context.resources.getString(R.string.new_), 0),
//            TypeModel(context.resources.getString(R.string.girly), 6),
//            TypeModel(context.resources.getString(R.string.lifestyle), 4),
            TypeModel(context.resources.getString(R.string.manda), 0)
//            TypeModel(context.resources.getString(R.string.animal), 2),
//            TypeModel(context.resources.getString(R.string.fantasy_world), 3))
//            TypeModel(context.resources.getString(R.string.people), 5)
        )
    }

    fun getAllType2(context: Context): ArrayList<TypeModel> {
        val checkNet = context!!.config.checkNetwork
        val list2: ArrayList<TypeModel> = ArrayList()
        if (checkNet == true) {
            val apiJson = context.config.category
            val gson = Gson()
            val category = gson.fromJson(apiJson, ColorBook::class.java)

            val position = category.size - 1
            list2.add(TypeModel(context.resources.getString(R.string.manda), 10002))
            for (i in 0..position) {
                val id = category[i].cate_id
                val idInt = id.toInt()
                list2.add(TypeModel(category[i].cate_name, idInt))

            }
        } else {
            list2.add(TypeModel(context.resources.getString(R.string.manda), 10002))
        }
        return list2

    }

    //    val listCats
//    val listDogs = loadDogs()

    val listMandala = loadMandala()
    val listHardMandalas = loadHardManalas()
    val listSimpleMandalas = loadSimpleManalas()

    private fun loadSimpleManalas(): ArrayList<ImageModel> {
        val list = arrayListOf<ImageModel>()
        for (i in 1..6) {
            val name = "simple_mandalas_$i"
            when {
                i <= 3 -> list.add(ImageModel(name = name, type = 1, date = "", time = "", urlGoc = ""))
                i in 4..6 -> list.add(ImageModel(name = name, type = 2, date = "", time = "", urlGoc = ""))
                else -> list.add(ImageModel(name = name, date = "", time = "", urlGoc = ""))
            }
        }
        return list
    }

    private fun loadHardManalas(): ArrayList<ImageModel> {
        val list = arrayListOf<ImageModel>()
        for (i in 1..10) {
            val name = "mandalaa_$i"
            when {
                i <= 3 -> list.add(ImageModel(name = name, type = 1, date = "", time = "", urlGoc = ""))
                i in 4..6 -> list.add(ImageModel(name = name, type = 2, date = "", time = "", urlGoc = ""))
                else -> list.add(ImageModel(name = name, date = "", time = "", urlGoc = ""))
            }
        }
        return list
    }
//   val listHouse = loadHouse()


    //type 0 normal
    //type 1 hot
    //type 2 new


    private val minMandala = 1
    private val maxMandala = 10
    private fun loadMandala(): ArrayList<ImageModel> {
        val list = arrayListOf<ImageModel>()
        list.add(ImageModel("mandalaa_1", type = 3, date = "", time = "", urlGoc = ""))
        list.add(ImageModel("simple_mandalas_1", type = 4, date = "", time = "", urlGoc = ""))
//        for (i in minMandala..maxMandala) {
//            val name = "mandalaa_$i"
//            when {
//                i <= 3 -> list.add(ImageModel(name = name, type = 1))
//                i in 4..6 -> list.add(ImageModel(name = name, type = 2))
//                else -> list.add(ImageModel(name = name))
//            }
//        }
        return list
    }


    val listAllImages = loadNames()
    private fun loadNames(): ArrayList<String> {
        val list = arrayListOf<String>()

        for (i in minMandala..maxMandala) {
            list.add("mandalaa_$i")
        }
//        for (i in minHou..maxHou) {
//            list.add("house_$i")
//        }
        return list
    }


    fun getIdRawFromName(context: Context, name: String): Int? {
        return context.resources.getIdentifier(name, "raw", context.packageName)
    }

    fun test(giatri: Int, completion: (Int) -> Unit) {
        Timer("SettingUp", false).schedule(1000) {
            completion(10)
        }
    }


    val PRIVATE_POLICY = "https://www.facebook.com/notes/volio-vietnam/coloring-book-privacy-policy/353525585367913/"

    val COLORS_CODE = Arrays.asList(
        "#9E9E9E",
        "#FFF9C4", "#FFF176", "#FFEB3B", "#FBC02D", "#F57F17", "#F5F5F5", "#E0E0E0",
        "#FFECB3", "#FFD54F", "#FFC107", "#FFA000", "#FF6F00",
        "#FFCCBC", "#FF8A65", "#FF5722", "#E64A19", "#BF360C",
        "#F0F4C3", "#DCE775", "#CDDC39", "#AFB42B", "#827717",
        "#DCEDC8", "#AED581", "#8BC34A", "#689F38", "#33691E",
        "#C8E6C9", "#81C784", "#4CAF50", "#388E3C", "#1B5E20",
        "#B2DFDB", "#4DB6AC", "#009688", "#00796B", "#004D40",
        "#B2EBF2", "#4DD0E1", "#00BCD4", "#0097A7", "#006064",
        "#BBDEFB", "#64B5F6", "#2196F3", "#1976D2", "#0D47A1",
        "#C5CAE9", "#7986CB", "#3F51B5", "#303F9F", "#1A237E",
        "#D1C4E9", "#9575CD", "#673AB7", "#512DA8", "#311B92",
        "#E1BEE7", "#BA68C8", "#9C27B0", "#7B1FA2", "#4A148C",
        "#F8BBD0", "#F06292", "#E91E63", "#C2185B", "#880E4F"
    )

    val listAllColor =
        arrayListOf(
            ListColorModel("Spring", arrayListOf("#EF238D", "#EA3136", "#FE6700", "#FE9900", "#FEFE00", "#01CC01", "#00AFEE", "#009899", "#003399", "#6600CD", "#633191", "#9A33CC", "#FFFFFF", "#000000"))
//            ListColorModel("Summer", arrayListOf("#fedc55", "#ffcb00", "#ff8a00", "#f85c00", "#fd7f7f", "#5fccff", "#1173d7", "#39c600", "#ff9975", "#e81e00", "#5885AF")),
//            ListColorModel("Autumn", arrayListOf("#f8c453", "#ff8a00", "#df6027", "#c23e2a", "#8d1818", "#727c1d", "#498edb", "#35559a", "#b86d15", "#804806", "#C15B78")),
//            ListColorModel("Winter", arrayListOf("#f5d8c5", "#98ddfe", "#61a8f0", "#2d79c5", "#119290", "#dabbed", "#ad7ffa", "#f89ae3", "#c2c2dd", "#ffffff", "#C15B78")),
//            ListColorModel("Relax", arrayListOf("#c0e4fd", "#86d7fe", "#72b0f5", "#b987ec", "#50c396", "#fff695", "#f0757f", "#ffa67a", "#fdadb6", "#74d2b7", "#C15B78")),
//            ListColorModel("Vivid", arrayListOf("#f97c01", "#1fc3ff", "#ff4e92", "#fd7ca2", "#5f2d89", "#b65a04", "#fee15f", "#ef3125", "#008af4", "#37edc7", "#C15B78")),
//            ListColorModel("Basic", arrayListOf("#f8ee53", "#f76735", "#ef3125", "#ffaac2", "#624fa1", "#0c96f7", "#0c57b5", "#2d731a", "#ffffff", "#e1b996", "#C15B78")),
//            ListColorModel("Vintage", arrayListOf("#e1b996", "#f6d69d", "#f3c367", "#df7552", "#ee9a99", "#a7f4f4", "#85d1d1", "#84b597", "#b08fbb", "#ad586f", "#C15B78")),
//
//            ListColorModel("Shade Of Dark", arrayListOf("#51355d", "#143d57", "#136151", "#571c2a", "#3c3310", "#4c3111", "#215c84", "#2d3e50", "#6f2435", "#401f4e", "#e1b996")),
//            ListColorModel("Lullaby", arrayListOf("#c21a1a", "#f48f97", "#ce1874", "#6f5089", "#8ddb9d", "#25a2a6", "#f5a10b", "#ce5621", "#e1bcaa", "#fe6f69", "#f97c01")),
//            ListColorModel("Sunset", arrayListOf("#fdf3b8", "#f7d16c", "#eb9549", "#ed92a4", "#cd80bc", "#abe6eb", "#78c1ca", "#7a8be4", "#696fc7", "#996ba4", "#40b24e"))
        )
    val listWaterColor =
        arrayListOf(
            ListColorModel("Spring", arrayListOf("#69bdd2", "#4d94ff", "#3385ff", "#1a75ff", "#6699ff", "#4d88ff", "#4d4dff", "#99d6ff", "#80ccff", "#66c2ff", "#4db8ff", "#1a8cff", "#0080ff", "#0073e6"))
        )
    val listcrayColor =
        arrayListOf(
            ListColorModel("Spring", arrayListOf("#9A9797", "#A9A9A9", "#e0e0d1", "#d6d6c2", "#ccccb3", "#c2c2a3", "#b8b894", "#708090", "#778899", "#696969", "#C0C0C0", "#D3D3D3", "#DCDCDC", "#808080"))
        )

    var bitmap: Bitmap? = null

    var tabChoose = -1
    var positionChoose = -1

    fun Context.shareApp() {
        val linkApk = getString(R.string.base_link_apk) + applicationContext!!.packageName
        val appName = getString(R.string.app_name)
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.run {
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))  // title
            putExtra(
                Intent.EXTRA_TEXT,
                "Download app " + getString(R.string.app_name) + ": $linkApk"  // content
            )
        }
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)))
    }

    fun saveBitmap(name: String, imageName: String, mBitmap: Bitmap) {
        val t1 = Thread(Runnable {
            val bytes = ByteArrayOutputStream()
            mBitmap.compress(Bitmap.CompressFormat.PNG, 30, bytes)
            val fo: FileOutputStream
            try {
                fo = FileOutputStream(name)
                fo.write(bytes.toByteArray())
                fo.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
        t1.start()
    }
}



