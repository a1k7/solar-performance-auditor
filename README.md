# Solar Performance Auditor

Ever looked at your solar inverter app and wondered whether "11 units today" is actually good? I did too. That's basically why I built this.

It's a small command-line tool that calculates how much your rooftop solar system *should* have generated on a particular day, compares it with what it actually generated, and tries to explain the difference — whether it's dust, heat, shading, an undersized inverter, or something else.

No GUI, no extra libraries, no unnecessary complexity. Just plain Java that runs in your terminal.

---

## Why this exists

My family got rooftop solar installed under the PM Surya Ghar scheme a couple of years ago. After the installation, we were given a login to an app that basically showed one big number — units generated. But there was no way to know what "normal" generation should look like.

If the output dropped by 15% because the panels were covered in dust or a new water tank started creating shade in the afternoon, we probably wouldn't notice.

Commercial monitoring tools exist, but many of them are tied to specific inverter brands and mostly just show graphs. I wanted something that could actually explain the problem in simple terms:

"You're 12% below where you should be. It probably hasn't rained recently, so dust could be the reason. You've lost around ₹380 this month."

So I built one. It's not meant to be a perfect solar simulator. It's a practical tool that tries to answer a simple question:

**Is my solar system performing as it should?**

---

## What you'll need

Java 17 or newer. Check with:

```bash
java -version

---

## License

MIT. Use it, fork it, change it, whatever.
