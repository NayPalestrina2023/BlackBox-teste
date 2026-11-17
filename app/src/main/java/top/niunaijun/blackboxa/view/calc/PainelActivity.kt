package top.niunaijun.blackboxa.view.calc

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.view.main.WelcomeActivity

class PainelActivity : Activity() {

    private var goingToEngine = false
    private val green = Color.parseColor("#39FF14")
    private val markGreen = Color.parseColor("#3DFF7F")
    private val match = ViewGroup.LayoutParams.MATCH_PARENT
    private val wrap = ViewGroup.LayoutParams.WRAP_CONTENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        setContentView(build())
    }

    override fun onResume() {
        super.onResume()
        goingToEngine = false
    }

    override fun onStop() {
        super.onStop()
        if (!goingToEngine && !isFinishing) finish()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun tv(t: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = t
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun build(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val iconBox = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#0D0D0D"))
                setStroke(dp(1), green)
            }
            clipToOutline = true
        }
        val img = ImageView(this).apply {
            setImageResource(R.drawable.ic_calc_foreground)
            scaleX = 1.9f
            scaleY = 1.9f
        }
        iconBox.addView(img, FrameLayout.LayoutParams(match, match))
        header.addView(iconBox, LinearLayout.LayoutParams(dp(72), dp(72)))
        val titles = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), 0, 0, 0)
        }
        titles.addView(tv("Naiane Ramos", 26f, green))
        titles.addView(tv("Analista e Desenvolvimento de Sistemas", 13f, Color.WHITE))
        header.addView(titles, LinearLayout.LayoutParams(0, wrap, 1f))
        root.addView(header, LinearLayout.LayoutParams(match, wrap))
        root.addView(View(this), LinearLayout.LayoutParams(match, dp(24)))

        val names = listOf("Aplicativos", "Fotos", "Vídeos", "Documentos", "Arquivos", "Configurações")
        for (pair in names.chunked(2)) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (name in pair) {
                val tile = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    background = GradientDrawable().apply {
                        cornerRadius = dp(16).toFloat()
                        setColor(Color.BLACK)
                        setStroke(dp(1), green)
                    }
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                }
                tile.addView(tv(name, 16f, Color.WHITE).apply { gravity = Gravity.CENTER })
                tile.addView(
                    tv(if (name == "Aplicativos") "toque para abrir" else "em breve", 12f, green)
                        .apply { gravity = Gravity.CENTER }
                )
                tile.setOnClickListener {
                    if (name == "Aplicativos") {
                        goingToEngine = true
                        startActivity(Intent(this, WelcomeActivity::class.java))
                    } else {
                        Toast.makeText(this, "$name: em breve", Toast.LENGTH_SHORT).show()
                    }
                }
                val lp = LinearLayout.LayoutParams(0, dp(100), 1f)
                lp.setMargins(dp(6), dp(6), dp(6), dp(6))
                row.addView(tile, lp)
            }
            root.addView(row, LinearLayout.LayoutParams(match, wrap))
        }

        root.addView(View(this), LinearLayout.LayoutParams(match, 0, 1f))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(Color.parseColor("#060B09"))
                setStroke(dp(2), markGreen)
            }
            setPadding(dp(16), dp(18), dp(16), dp(18))
        }
        fun line(t: String, size: Float, color: Int, bold: Boolean, top: Int) {
            val v = tv(t, size, color, bold).apply { gravity = Gravity.CENTER_HORIZONTAL }
            card.addView(v, LinearLayout.LayoutParams(match, wrap).apply { topMargin = dp(top) })
        }
        line("NR SYSTEMS®", 26f, markGreen, true, 0)
        line("Naiane Ramos da Silva", 17f, Color.WHITE, true, 10)
        line("ANALISTA DE SISTEMAS", 13f, markGreen, false, 10)
        line("ANÁLISE E DESENVOLVIMENTO DE SISTEMAS", 11f, Color.parseColor("#A9B3AE"), false, 8)
        line("© 2026 · Todos os direitos reservados.", 11f, Color.parseColor("#7E8A84"), false, 8)
        root.addView(card, LinearLayout.LayoutParams(match, wrap))

        val exit = tv("Sair e voltar à calculadora", 16f, Color.BLACK).apply {
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(26).toFloat()
                setColor(green)
            }
            setOnClickListener {
                startActivity(Intent(this@PainelActivity, CalculatorActivity::class.java))
                finish()
            }
        }
        root.addView(exit, LinearLayout.LayoutParams(match, dp(52)).apply { topMargin = dp(16) })
        return root
    }
}
