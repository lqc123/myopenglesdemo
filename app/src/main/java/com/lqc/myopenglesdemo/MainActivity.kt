package com.lqc.myopenglesdemo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.lqc.myopenglesdemo.render.CameraTextureRender
import com.lqc.myopenglesdemo.render.SimpleRender
import com.lqc.myopenglesdemo.render.TextureRender
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler)

        val datas: Array<Item> = arrayOf(
            Item(
                "图片纹理",
                TextureRender::class.java.name
            ),
            Item(
                "相机",
                CameraTextureRender::class.java.name
            ),
            Item(
                "本地绘图",
                NativeActivity::class.java.name
            ),
            Item(
                "简单图形",
                SimpleRender::class.java.name
            )
        )
//        val datas= ArrayList<Item>()
//        for (i in 1..29) {
//            datas.add(Item(i.toString(),i.toString()))
//        }
        AndPermission.with(this)
            .runtime()
            .permission(Permission.CAMERA,Permission.READ_EXTERNAL_STORAGE)
            .onGranted { granted: List<String?>? -> }
            .onDenied { denied: List<String?>? -> }
            .start()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
                super.onViewRecycled(holder)
                println("onViewRecycled-->>" + holder.layoutPosition)
            }

            override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
                super.onViewDetachedFromWindow(holder)
                println("onViewDetachedFromWindow-->>" + holder.layoutPosition)
            }


            override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
                super.onViewAttachedToWindow(holder)
                println("onViewAttachedToWindow-->>" + holder.layoutPosition)
            }


            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                val textView = Button(parent.context)
                textView.setBackgroundColor(Color.WHITE)
                val layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.topMargin = 20
                textView.layoutParams = layoutParams

                return object : RecyclerView.ViewHolder(textView) {

                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item: Button = holder.itemView as Button
                item.text = datas[position].typeName
                item.setOnClickListener {
                    if (datas[position].type.toLowerCase().contains("activity")) {
                        it.context.startActivity(
                            Intent(
                                it.context,
                                Class.forName(datas[position].type)
                            )
                        )
                    } else {
                        TextureActivity.start(
                            it.context,
                            datas[position].type
                        )
                    }
                }
            }

            override fun getItemCount(): Int {
                return datas.size
            }
        }

        recyclerView.setAdapter(adapter)
    }

    data class Item(val typeName: String, val type: String)


}
