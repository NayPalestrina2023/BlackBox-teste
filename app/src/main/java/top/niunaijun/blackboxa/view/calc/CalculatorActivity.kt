package top.niunaijun.blackboxa.view.calc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val NEON = Color.parseColor("#39FF14")
private val DARK = Color.parseColor("#1C1C1C")
private const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
private const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

class CalculatorActivity : Activity() {

    private lateinit var store: Store
    private var expr = ""
    private var result = ""
    private var justEvaluated = false
    private var exprView: TextView? = null
    private var resultView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        store = Store(applicationContext)
        if (store.isSet()) showCalculator() else showSetup()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun shape(color: Int, radius: Int, stroke: Int? = null) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }

    private fun label(text: String, size: Float, color: Int) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
    }

    private fun button(text: String, bg: Int, fg: Int, onClick: () -> Unit) = TextView(this).apply {
        this.text = text
        textSize = 18f
        setTextColor(fg)
        gravity = Gravity.CENTER
        background = shape(bg, 26)
        setOnClickListener { onClick() }
    }

    private fun field(hint: String) = EditText(this).apply {
        this.hint = hint
        setHintTextColor(Color.GRAY)
        setTextColor(Color.WHITE)
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        transformationMethod = PasswordTransformationMethod.getInstance()
        filters = arrayOf(InputFilter.LengthFilter(8))
        background = shape(Color.BLACK, 10, Color.GRAY)
        setPadding(dp(14), dp(12), dp(14), dp(12))
        layoutParams = LinearLayout.LayoutParams(MATCH, WRAP).apply { bottomMargin = dp(12) }
    }

    private fun showSetup() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(dp(24), dp(24), dp(24), dp(24))
            gravity = Gravity.CENTER_VERTICAL
        }
        root.addView(label("Criar senha privada", 26f, NEON))
        root.addView(
            label("Escolha uma senha com exatamente 8 números. Depois, digite essa senha na calculadora para entrar.", 14f, Color.WHITE)
                .apply { setPadding(0, dp(8), 0, dp(20)) }
        )
        val first = field("Senha (8 números)")
        val second = field("Repita a senha")
        val error = label("", 14f, Color.parseColor("#FF5252"))
        root.addView(first)
        root.addView(second)
        root.addView(error)
        val save = button("Salvar senha", NEON, Color.BLACK) {
            val a = first.text.toString()
            val b = second.text.toString()
            when {
                a.length != 8 -> error.text = "A senha precisa ter exatamente 8 números."
                a != b -> error.text = "As senhas não são iguais."
                else -> {
                    store.save(a)
                    showCalculator()
                }
            }
        }
        root.addView(save, LinearLayout.LayoutParams(MATCH, dp(52)).apply { topMargin = dp(16) })
        setContentView(root)
    }

    private fun showCalculator() {
        expr = ""
        result = ""
        justEvaluated = false
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        val display = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.END
        }
        val ev = label("", 40f, Color.WHITE).apply { gravity = Gravity.END; maxLines = 2 }
        val rv = label("", 28f, NEON).apply { gravity = Gravity.END }
        exprView = ev
        resultView = rv
        display.addView(ev, LinearLayout.LayoutParams(MATCH, WRAP))
        display.addView(rv, LinearLayout.LayoutParams(MATCH, WRAP))
        root.addView(display, LinearLayout.LayoutParams(MATCH, 0, 1f))

        val rows = listOf(
            listOf("C", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )
        val ops = listOf("C", "⌫", "%", "÷", "×", "-", "+")
        for (keys in rows) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (k in keys) {
                val isEq = k == "="
                val fg = if (isEq) Color.BLACK else if (k in ops) NEON else Color.WHITE
                val b = button(k, if (isEq) NEON else DARK, fg) { press(k) }
                b.textSize = 26f
                val lp = LinearLayout.LayoutParams(0, dp(72), if (k == "0") 2f else 1f)
                lp.setMargins(dp(5), dp(5), dp(5), dp(5))
                row.addView(b, lp)
            }
            root.addView(row, LinearLayout.LayoutParams(MATCH, WRAP))
        }
        setContentView(root)
    }

    private fun refresh() {
        exprView?.text = expr
        resultView?.text = result
    }

    private fun press(key: String) {
        when (key) {
            "C" -> { expr = ""; result = ""; justEvaluated = false }
            "⌫" -> { expr = expr.dropLast(1); result = ""; justEvaluated = false }
            "=" -> {
                if (expr.isNotEmpty()) {
                    val r = Calc.evaluate(expr)
                    if (r == null) {
                        result = "Erro"
                    } else {
                        result = r
                        expr = r
                        justEvaluated = true
                    }
                }
            }
            "+", "-", "×", "÷" -> {
                justEvaluated = false
                if (expr.isEmpty()) {
                    if (key == "-") expr = "-"
                } else if (expr.last() in "+-×÷") {
                    expr = expr.dropLast(1) + key
                } else {
                    expr += key
                }
            }
            else -> {
                if (justEvaluated && key != "%") {
                    expr = key
                    result = ""
                } else {
                    expr += key
                }
                justEvaluated = false
                if (key[0].isDigit()) {
                    val tail = expr.takeLast(8)
                    if (tail.length == 8 && tail.all { it.isDigit() } && !store.isLocked()) {
                        if (store.verify(tail)) {
                            store.registerSuccess()
                            expr = ""
                            result = ""
                            refresh()
                            startActivity(Intent(this, PainelActivity::class.java))
                            finish()
                            return
                        } else {
                            val before = expr.length > 8 && expr[expr.length - 9].isDigit()
                            if (!before) store.registerFail()
                        }
                    }
                }
            }
        }
        refresh()
    }
}

private class Store(context: Context) {
    private val prefs = context.getSharedPreferences("privaci_auth", Context.MODE_PRIVATE)
    private val lockPrefs = context.getSharedPreferences("privaci_lock", Context.MODE_PRIVATE)

    fun isSet() = prefs.contains("hash") && prefs.contains("salt")

    fun save(password: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("hash", Base64.encodeToString(derive(password, salt), Base64.NO_WRAP))
            .apply()
    }

    fun verify(password: String): Boolean {
        val s = prefs.getString("salt", null) ?: return false
        val h = prefs.getString("hash", null) ?: return false
        val salt = Base64.decode(s, Base64.NO_WRAP)
        return MessageDigest.isEqual(Base64.decode(h, Base64.NO_WRAP), derive(password, salt))
    }

    fun isLocked(): Boolean = System.currentTimeMillis() < lockPrefs.getLong("until", 0L)

    fun registerFail() {
        val fails = lockPrefs.getInt("fails", 0) + 1
        if (fails >= 5) {
            val level = lockPrefs.getInt("level", 0)
            val minutes = 5L shl minOf(level, 6)
            lockPrefs.edit()
                .putInt("fails", 0)
                .putInt("level", level + 1)
                .putLong("until", System.currentTimeMillis() + minutes * 60_000L)
                .apply()
        } else {
            lockPrefs.edit().putInt("fails", fails).apply()
        }
    }

    fun registerSuccess() {
        lockPrefs.edit().clear().apply()
    }

    private fun derive(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}

private object Calc {
    fun evaluate(input: String): String? = try {
        val p = Parser(input)
        val v = p.expression()
        if (!p.atEnd() || v.isNaN() || v.isInfinite()) null
        else BigDecimal(v).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    } catch (e: Exception) {
        null
    }

    private class Parser(val s: String) {
        var i = 0
        fun atEnd() = i >= s.length

        fun expression(): Double {
            var v = term()
            while (i < s.length && (s[i] == '+' || s[i] == '-')) {
                val op = s[i++]
                val r = term()
                v = if (op == '+') v + r else v - r
            }
            return v
        }

        fun term(): Double {
            var v = factor()
            while (i < s.length && (s[i] == '×' || s[i] == '÷')) {
                val op = s[i++]
                val r = factor()
                if (op == '÷') {
                    if (r == 0.0) throw ArithmeticException()
                    v /= r
                } else {
                    v *= r
                }
            }
            return v
        }

        fun factor(): Double {
            if (i < s.length && s[i] == '-') {
                i++
                return -factor()
            }
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
            if (start == i) throw IllegalArgumentException()
            var v = s.substring(start, i).toDouble()
            while (i < s.length && s[i] == '%') {
                i++
                v /= 100
            }
            return v
        }
    }
}
