package com.su.sharkmanfunc

import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class PermissionBottomSheetFragment(@BottomSheetBehavior.State val bottomSheetState: Int = BottomSheetBehavior.STATE_COLLAPSED) :
    BottomSheetDialogFragment() {

    private lateinit var perExit: TextView
    private lateinit var perWindowReq: TextView
    private lateinit var perStartUse: TextView

    companion object {
        fun open(activity: FragmentActivity): PermissionBottomSheetFragment {
            return PermissionBottomSheetFragment(BottomSheetBehavior.STATE_EXPANDED).apply {
                isCancelable = false
                show(
                    activity.supportFragmentManager,
                    ::PermissionBottomSheetFragment.name
                )
            }
        }
    }

    private val permissions = mapOf(
        "2. 锁屏显示权限" to "锁屏时支持显示",
        "3. 后台显示权限" to "在非当前APP界面插入充电器时显示动画",
        "4. 推荐设置" to "锁定后台+关闭电池优化"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.permission_bottom_sheet_fragment_layout, container).apply {
            //bind
            perStartUse = findViewById(R.id.per_start_use)
            perStartUse.setOnClickListener { dismiss() }

            perExit = findViewById(R.id.per_exit)
            perExit.apply {
                setOnClickListener {
                    requireActivity().finish()
                }
                paint.apply {
                    flags = Paint.UNDERLINE_TEXT_FLAG
                    isAntiAlias = true
                }
            }
            perWindowReq = findViewById(R.id.per_window_req)
            perWindowReq.setOnClickListener {
                if (!PermissionUtils.INS.checkWindowPermission(requireContext()))
                    PermissionUtils.INS.requestWindowPermission(this@PermissionBottomSheetFragment)
                else {
                    Toast.makeText(requireContext(), "已授予该权限", Toast.LENGTH_LONG).show()
                    syncPermissionStatus()
                }
            }
            //添加权限说明
            findViewById<LinearLayout>(R.id.per_pers).apply {
                permissions.forEach {
                    addView(
                        inflater.inflate(R.layout.permission_item_layout, container, false).apply {
                            findViewById<TextView>(R.id.per_title)?.text = it.key
                            findViewById<TextView>(R.id.per_desc)?.text = it.value
                        })
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            if (this is BottomSheetDialog) {
                behavior.apply {
                    this@PermissionBottomSheetFragment.behavior = this
                    state = bottomSheetState
                }
            }
        }
    }

    var behavior: BottomSheetBehavior<FrameLayout>? = null

    private fun syncPermissionStatus() {
        val res = PermissionUtils.INS.checkWindowPermission(requireContext())
        perWindowReq.isEnabled = !res
        if (res) {
            MainScope().launch {
                val ts = getString(R.string.start_use_text)
                (5 downTo 1).asFlow()
                    .onCompletion {
                        perStartUse.apply {
                            isEnabled = true
                            text = ts
                        }
                    }
                    .collect {
                        perStartUse.apply {
                            isEnabled = false
                            text = "$ts($it)"
                        }
                        delay(1000)
                    }
            }
        } else
            perStartUse.isEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PermissionUtils.WINDOW_REQUEST_CODE -> syncPermissionStatus()
        }
    }

}