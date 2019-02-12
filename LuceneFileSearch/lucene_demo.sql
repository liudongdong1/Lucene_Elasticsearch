/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50505
Source Host           : localhost:3306
Source Database       : lucene-demo

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2017-05-04 21:15:41
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `blog`
-- ----------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `tags` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `content` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of blog
-- ----------------------------
INSERT INTO `blog` VALUES ('1', 'Android原生与JS交互总结', 'android js', 'http://blog.liuxianan.com/android-native-js-interactive.html', '[TOC]\r\n\r\n# 前言\r\n\r\n这里说的交互，指的是采用官方提供的方法，其它实现方式（如拦截url，拦截prompt）不在本文描述范围之内。\r\n\r\n# JS调用原生\r\n\r\n通过给方法添加`@JavascriptInterface`注解，然后通过`mWebView.addJavascriptInterface(object, name)`将刚才那个方法所在的类注入JS，然后js就可以直接通过`name.方法名()`来调用刚才那个方法。\r\n\r\n## 示例\r\n\r\n获取包名：\r\n\r\n```java\r\nmWebView.addJavascriptInterface(new Object()\r\n{\r\n	@JavascriptInterface\r\n	public String getPackageName()\r\n	{\r\n		return mWebView.getContext().getPackageName();\r\n	}\r\n}, \"test\");\r\n```\r\n然后在js里面执行test.getPackageName()就可以获取当前apk的包名了：\r\n\r\n![](http://image.liuxianan.com/201607/20160705_144142_077_1827.png)\r\n\r\n上面偷懒直接采用匿名内部类的方式，正常情况下一般不会这么写。\r\n\r\n## 重载与参数类型转换\r\n\r\n众所周知，`js`不支持重载，`Java`有，那么注入到`js`的`java`方法是否支持重载呢？\r\n\r\n下面以获取`versionCode`和`versionName`为例来验证注入JS时的重载与类型转换问题。\r\n\r\n### 测试代码\r\n\r\n```java\r\n//TestJavaScript.java\r\npublic class TestJavaScript\r\n{\r\n	private Activity mActivity;\r\n	\r\n	public TestJavaScript(Activity mActivity)\r\n	{\r\n		this.mActivity = mActivity;\r\n	}\r\n\r\n	/**\r\n	 * 获取版本号\r\n	 * @return\r\n	 */\r\n	@JavascriptInterface\r\n	public String getVersion() throws NameNotFoundException\r\n	{\r\n		Log.i(\"info\", \"进入第1个getVersion方法\");\r\n		return getVersion(null);\r\n	}\r\n	\r\n	/**\r\n	 * 获取版本号\r\n	 * @return\r\n	 */\r\n	@JavascriptInterface\r\n	public String getVersion(String type) throws NameNotFoundException\r\n	{\r\n		Log.i(\"info\", \"进入第2个getVersion方法\");\r\n		Log.i(\"info\", \"type:\"+type);\r\n		if(type == null || \"\".equals(type)) type = \"name\";\r\n		PackageInfo info = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);\r\n		if(\"name\".equals(type)) return info.versionName;\r\n		else if(\"code\".equals(type)) return info.versionCode+\"\";\r\n		else return \"type error\";\r\n	}\r\n}\r\n\r\n//WelcomeActivity.java\r\nmWebView.addJavascriptInterface(new TestJavaScript(this), \"test\");\r\n```\r\n\r\n**说明**：type为`name`时返回`versionName`，type为`code`时返回`versionCode`，当然这里这样写仅仅是为了测试，实际环境中这样写的人肯定是找骂，哈哈。\r\n\r\n### 开始测试\r\n\r\n以上例子测试代码：\r\n\r\n```javascript\r\ntest.getVersion(); // 返回1.0 进入第1个getVersion方法\r\ntest.getVersion(null); // 返回1.0 进入第2个getVersion方法，获取到的type为null\r\ntest.getVersion(undefined); // 返回 type error，进入第2个方法，获取到的type为字符串形式的\"undefined\"\r\ntest.getVersion(\'name\'); // 返回1.0,\r\ntest.getVersion(\'code\'); // 返回1\r\ntest.getVersion(\'code\', 1); //提示 Error calling method on NPObject 错误\r\ntest.getVersion(222); // 提示 type error，进入第二个方法，获取到的是字符串的\"222\"\r\n```\r\n\r\n![](http://image.liuxianan.com/201607/20160705_151919_431_6589.png)\r\n\r\n假如只有下面这一个方法：\r\n\r\n```java\r\n@JavascriptInterface\r\npublic String getVersion(int a)\r\n{\r\n	Log.i(\"info\", \"进入int型getVersion方法，参数：\" + a);\r\n	return \"进入int型getVersion方法\";\r\n}\r\n```\r\n测试时：\r\n\r\n	test.getVersion(123); // 接收到参数为123\r\n	test.getVersion(\'abc\'); // 依然可以进入这个int型的方法，但是接收到的参数为0\r\n\r\n假如只有下面这2个方法：\r\n```java\r\n@JavascriptInterface\r\npublic String getVersion(String a)\r\n{\r\n	Log.i(\"info\", \"进入String型getVersion方法，参数：\" + a);\r\n	return \"进入String型getVersion方法\";\r\n}\r\n@JavascriptInterface\r\npublic String getVersion(int a)\r\n{\r\n	Log.i(\"info\", \"进入int型getVersion方法，参数：\" + a);\r\n	return \"进入int型getVersion方法\";\r\n}\r\n```\r\n测试时：\r\n\r\n	test.getVersion(123); // 进入int型方法，接收到参数为123\r\n	test.getVersion(\'abc\'); // 进入string型方法，接收到参数为\'abc\'\r\n\r\n再假设有如下代码：\r\n\r\n```java\r\n@JavascriptInterface\r\npublic void testParam(String param1, String param2)\r\n{\r\n	System.out.println(param1 == null ? \"@NULL\" : param1);\r\n	System.out.println(param2 == null ? \"@NULL\" : param2);\r\n}\r\n```\r\n\r\njs测试代码：\r\n\r\n```javascript\r\ntest.testParam(\'abc\'); // Error: Error calling method on NPObject.\r\ntest.testParam(\'abc\', undefined); // js没报错，后台输出 abc undefined（注意这个undefined是加了双引号的）\r\ntest.testParam(\'abc\', null); // js没报错，后台输出 abc @NULL\r\ntest.testParam(\'abc\', \'cbd\'); // js 没报错，后台输出 abc cbd\r\n```\r\n\r\n还有一些小测试，懒得逐一贴代码了，直接上结论。\r\n\r\n### 重要结论\r\n\r\nJava注入js时是区分重载的，但是由于存在类型转换，所以并不会完全像Java那样严格区分，换句话说，对于参数个数的不同会严格区分（前后端参数个数不同甚至会报错），但是对于参数类型的不同则没那么严格，如果能够找到正确类型的方法，那么会优先进入这个方法，如果找不到，则进入参数个数相同、类型不同的其它方法，会自动进行类型转换。\r\n\r\n### 参数类型转换规则\r\n\r\n假如只有一个参数类型是String的方法：\r\n\r\n* `null` 自动转 java 的 `null`；\r\n* `undefined` 自动转 java 的 字符串 `\"undefined\"`；\r\n* `123` 自动转 `\"123\"`；\r\n* `true` 自动转 `\"true\"`；\r\n* `\'abc\'` 转换正常的 `\"abc\"`；\r\n\r\n假如只有一个参数类型是int的方法：\r\n\r\n* `null` 自动转 `0`；\r\n* `undefined` 自动转 `0`；\r\n* `\'abc\'` 自动转 `0`；\r\n* `true/false` 全部自动转 `0`；\r\n* `123` 转换正常的 `123`；\r\n\r\n其它类型，比如时间、对象等就没测试了，因为实际使用中，string和int这2个足够用了。\r\n\r\n为避免一些不必要的麻烦，**建议注入方法时少用重载，尽量避免不同类型参数自动转换**，毕竟多一事不如少一事，多取几个名字就是了。\r\n\r\n## Error calling method on NPObject\r\n\r\n错误如下：\r\n\r\n	Error: Error calling method on NPObject.\r\n\r\n如果是在调用原生方法出现这个错误，一般有3个原因：\r\n\r\n1. 由于参数个数不正确导致的，上面的例子也提到了，假如方法只有1个参数，但是你传了2个参数，或者定义了2个参数，但是你只传了1个参数，那么就会报这个错误；\r\n2. 代码必须运行在UI线程中（如调用`mWebView.goBack()`），即`mActivity.runOnUiThread`；\r\n3. 原生方法内部出错，比如常见的空指针异常等，都会引起这个错误；\r\n\r\n## 切勿使用包装类型\r\n\r\n假设有如下方法：\r\n\r\n```java\r\n@JavascriptInterface\r\npublic String testInt(Integer a)\r\n{\r\n	return \"int:\"+a;\r\n}\r\n\r\n@JavascriptInterface\r\npublic String testBoolean(Boolean a)\r\n{\r\n	return \"boolean:\"+a;\r\n}\r\n```\r\n调用：\r\n\r\n```java\r\ntest.testInt(123); // 输出\"int:null\"\r\ntest.testBoolean(true); // 输出\"boolean:null\"\r\ntest.testBoolean(false); // 输出\"boolean:null\"\r\n```\r\n\r\n可以发现，如果Android这边参数使用了包装类型会导致参数接收不到，必须使用基本类型，把上面的Integer和Boolean换成int和boolean就没问题了。\r\n\r\n## 注入有效期\r\n\r\n只需要对webview全局注入一次，无需针对每个页面重新注入，一次注入“永久”生效。\r\n\r\n有人会问，为啥cordova/phonegap、mui等都必须在某一事件触发之后才能调用原生提供的方法呢？也就是为啥它们是针对具体页面注入的？这个是因为它们实现机制和我们这里不一样，而且还有很多其它方面考虑，后续有机会再着重讨论这个问题。\r\n\r\n## JavascriptInterface注解漏洞\r\n\r\nAndroid4.2开始增加`@JavascriptInterface`注解，目的是为了解决一个 [漏洞](http://www.wooyun.org/bugs/wooyun-2010-036131/auth/3488a336fe230cb6928cf2444a53663f) ，在4.2版本之后，只有添加了这个注解的方法才会被注入JS。\r\n\r\n## iframe问题\r\n\r\n对iframe的支持不同设备不一样，有的会注入到iframe里面，有的不会。\r\n\r\n//TODO 本小节有待完善。\r\n\r\n## 远程调试提示问题\r\n\r\n即使对象注入成功了，在控制台输入的时候，test会有提示，但是`test.`再往后面就没有提示了，这是正常现象，不要以为没有注入成功。\r\n\r\n# 原生调用JS\r\n\r\n一般都是通过`mWebView.loadUrl(\'javascript:xxx\')`来执行一段JS，据说这个方法有一个bug，就是执行的时候如果输入法是弹出的，执行后输入法会自动消失，我暂未亲测。\r\n\r\n这个方法最大的缺点是无法优雅的解决异步回调问题，鉴于此，Android4.4开始增加了如下方法：\r\n\r\n	mWebView.evaluateJavascript(script, resultCallback)\r\n\r\n有了这个方法就可以非常方便的实现js的异步回调，但是毕竟低于Android4.4的版本还是有比较大的份额，所以一般还是得自己另行解决异步回调的问题。\r\n\r\n//TODO 这里还有待完善');
INSERT INTO `blog` VALUES ('2', '【干货】JS版汉字与拼音互转终极方案，附简单的JS拼音输入法', 'js 拼音', 'http://blog.liuxianan.com/pinyinjs.html', '# 前言\r\n\r\n网上关于JS实现汉字和拼音互转的文章很多，但是比较杂乱，都是互相抄来抄去，而且有的不支持多音字，有的不支持声调，有的字典文件太大，还比如有时候我仅仅是需要获取汉字拼音首字母却要引入200kb的字典文件，无法根据实际需要满足需求。\r\n\r\n综上，我精心整理并修改了网上几种常见的字典文件并简单封装了一下可以直接拿来用的工具库。\r\n\r\n> 这篇文章差不多一个月前就写好了大部分了，但是就差拼音输入法这一块一直没时间去弄(与其说是没时间，还不如说是本人太懒)，所以一直拖到今天才发表。\r\n\r\n# 代码和DEMO演示\r\n\r\ngithub项目地址：https://github.com/liuxianan/pinyinjs\r\n\r\n完整demo演示：http://demo.liuxianan.com/pinyinjs/\r\n\r\n带多音字识别的演示：http://demo.liuxianan.com/pinyinjs/polyphone.html\r\n\r\n汉字转拼音：\r\n\r\n![](http://image.liuxianan.com/201610/20161018_101128_427_0451.gif)\r\n\r\n带词库的识别多音字的汉字转拼音：\r\n\r\n![](http://image.liuxianan.com/201610/20161018_101130_427_0451.gif)\r\n\r\n拼音转汉字（简单的拼音输入法）：\r\n\r\n![](http://image.liuxianan.com/201610/20161018_101129_427_0451.gif)\r\n\r\n# 关于多音字\r\n\r\n鉴于很多人都比较关心多音字的问题，所以单独拿出一个小章节来介绍多音字的相关问题。\r\n\r\n准确识别各种复杂语句中混杂的多音字其实并没有那么容易，有两个关键的地方，一个是多音字词库的丰富程度，一个是能否正确的给语句进行分词。而词库和分词的实现都需要一个非常丰富的`词典`文件，现代汉语词语有多少个，估计没有人算得清，再加上每天新出现的人名、网络词语、科技词语等等。一个普通的词库文件至少也有几百kb，所以不太适合web环境下去实现，一般最好放在服务器端做成一个接口。\r\n\r\n鉴于很多人都希望有多音字识别的功能，所以我简单实现了一个版本。词库文件是从[这里](https://github.com/hotoo/pinyin/tree/master/data/phrases-dict.js)找到的，并根据实际情况将文件从`1.8M`压缩到了`912kb`，分词暂时只是自己非常简单的实现(也可以说压根就没有分词)，如果是服务器端推荐几个不错的中文分词工具：Python版的[Jieba](https://github.com/fxsjy/jieba)和[NodeJieba](https://github.com/yanyiwu/nodejieba)，性能非常好，其它语言版的参考上面项目的README。\r\n\r\n关于分词，摘抄一段网络解释：\r\n\r\n> 词是最小的能够独立活动的有意义的语言成分，英文单词之间是以空格作为自然分界符的，而汉语是以字为基本的书写单位，词语之间没有明显的区分标记，因此，要对中文信息进行处理，正确的分词就显得尤为关键。\r\n\r\n比如`看中国`这一个词，单独的`看中`读`kàn zhòng`，`中国`读`zhōng guó`，连在一起却读作`kàn zhōng guó`。\r\n\r\n我这个实现非常得简单，效果一般，性能也一般，需要下载将近1M的词库文件，所以不适合web环境，演示地址：\r\n\r\nhttp://demo.liuxianan.com/pinyinjs/polyphone.html\r\n\r\n# 汉字与拼音相关知识普及\r\n\r\n## 汉字范围\r\n\r\n一般认为Unicode编码中的汉字范围是 `/^[\\u2E80-\\u9FFF]+$/`(11904-40959)，但是其中有很多不是汉字，或者说是可以读的汉字，本文用到的几个字典文件的汉字范围均是 `/^[\\u4E00-\\u9FA5]+$/`，也就是(19968-40869)，另外还有一个单独的汉字〇，其Unicode位置是12295。\r\n\r\n## 拼音组合\r\n\r\n汉字有21个声母：b, p, m, f, d, t, n, l, g, k, h, j, q, x, zh, ch, sh, r, z, c, s，24个韵母，其中单韵母有6个：a, o, e, i, u, v, 复韵母有18个：ai , ei,  ui , ao,  ou,  iu , ie, ve,  er,  an , en , in,  un , vn , ang, eng,  ing , ong，假设声母和韵母两两组合的话，会有24X21=504种组合，实际情况是有些组合是没有意义的，比如bv, gie, ve等，去除这部分后，还剩余412种。\r\n\r\n# 拼音字典文件\r\n\r\n按照字典文件的大小从小到大依次介绍。\r\n\r\n## 字典一：拼音首字母\r\n\r\n该[字典文件](https://github.com/liuxianan/pinyinjs/blob/master/pinyin_dict_firstletter.js)的内容大致如下：\r\n\r\n```javascript\r\n/**\r\n * 拼音首字母字典文件\r\n */\r\nvar pinyin_dict_firstletter = {};\r\npinyin_dict_firstletter.all = \"YDYQSXMWZSSXJBYMGCCZQPSSQBYCDSCDQLDYLYBSSJG...\";\r\npinyin_dict_firstletter.polyphone = {\"19969\":\"DZ\",\"19975\":\"WM\",\"19988\":\"QJ\",\"20048\":\"YL\",...};\r\n```\r\n\r\n该数据字典将Unicode字符中`4E00`(19968)-`9FA5`(40869)共计20902个汉字的拼音首字母拼接在一起得到一个很长的字符串，然后再将有多音字的汉字（共计370个多音字）单独列出来。该字典文件大小为`25kb`。\r\n\r\n该字典文件优点是体积小，支持多音字，缺点是只能获取拼音首字母。\r\n\r\n## 字典二：常用汉字\r\n\r\n首先，从网络上找到如下字典文件：该字典文件将汉字按照拼音进行归类，共计401种组合，收录了6763个常用汉字，文件体积只有24kb，不支持多音字。\r\n\r\n字典文件大致内容如下（这里只是示例，所以只展示一小部分）：\r\n\r\n```javascript\r\n/**\r\n * 常规拼音数据字典，收录常见汉字6763个，不支持多音字\r\n */\r\nvar pinyin_dict_notone = \r\n{\r\n	\"a\":\"啊阿锕\",\r\n	\"ai\":\"埃挨哎唉哀皑癌蔼矮艾碍爱隘诶捱嗳嗌嫒瑷暧砹锿霭\",\r\n	\"an\":\"鞍氨安俺按暗岸胺案谙埯揞犴庵桉铵鹌顸黯\",\r\n	\"ang\":\"肮昂盎\",\r\n	\"ao\":\"凹敖熬翱袄傲奥懊澳坳拗嗷噢岙廒遨媪骜聱螯鏊鳌鏖\",\r\n	\"ba\":\"芭捌扒叭吧笆八疤巴拔跋靶把耙坝霸罢爸茇菝萆捭岜灞杷钯粑鲅魃\",\r\n	\"bai\":\"白柏百摆佰败拜稗薜掰鞴\",\r\n	\"ban\":\"斑班搬扳般颁板版扮拌伴瓣半办绊阪坂豳钣瘢癍舨\",\r\n	\"bang\":\"邦帮梆榜膀绑棒磅蚌镑傍谤蒡螃\",\r\n	\"bao\":\"苞胞包褒雹保堡饱宝抱报暴豹鲍爆勹葆宀孢煲鸨褓趵龅\",\r\n	\"bo\":\"剥薄玻菠播拨钵波博勃搏铂箔伯帛舶脖膊渤泊驳亳蕃啵饽檗擘礴钹鹁簸跛\",\r\n	\"bei\":\"杯碑悲卑北辈背贝钡倍狈备惫焙被孛陂邶埤蓓呗怫悖碚鹎褙鐾\",\r\n	\"ben\":\"奔苯本笨畚坌锛\"\r\n	// 省略其它\r\n};\r\n```\r\n\r\n后来慢慢发现这个字典文件中存在诸多错误，比如把`虐`的拼音写成了`nue`（正确写法应该是nve）,`躺`写成了`thang`，而且不支持多音字，所以后来我根据其它字典文件自己重新生成了一份这样格式的 [字典文件](https://github.com/liuxianan/pinyinjs/blob/master/pinyin_dict_notone.js)：\r\n\r\n* 共有404种拼音组合\r\n* 收录了6763个常用汉字\r\n* 支持多音字\r\n* 不支持声调\r\n* 文件大小为27kb\r\n\r\n同时，我根据网上一份[常用6763个汉字使用频率表](http://blog.sina.com.cn/s/blog_5e2ffb490100dnfg.html)，将这6763个汉字按照使用频率进行了排序，这样就可以实现一个差强人意的JS版输入法了。\r\n\r\n另外，根据另外一份更完整的字典文件发现其实共有412种拼音组合，上面字典文件中没有出现的8种发音是：\r\n\r\n	chua,den,eng,fiao,m,kei,nun,shei\r\n\r\n## 字典三：终极字典\r\n\r\n首先，从网上找的如下结构字典文件（下面称为字典A），具体是哪不记得了，支持声调和多音字，它将Unicode字符中`4E00`(19968)-`9FA5`(40869)共计20902个汉字（如果算上〇的话那就是20903个）拼音全部列举，该字典文件大小为`280kb`：\r\n\r\n```\r\n3007 (ling2)\r\n4E00 (yi1)\r\n4E01 (ding1,zheng1)\r\n4E02 (kao3)\r\n4E03 (qi1)\r\n4E04 (shang4,shang3)\r\n4E05 (xia4)\r\n4E06 (none0)\r\n4E07 (wan4,mo4)\r\n4E08 (zhang4)\r\n4E09 (san1)\r\n4E0A (shang4,shang3)\r\n4E0B (xia4)\r\n4E0C (ji1)\r\n4E0D (bu4,bu2,fou3)\r\n4E0E (yu3,yu4,yu2)\r\n4E0F (mian3)\r\n4E10 (gai4)\r\n4E11 (chou3)\r\n4E12 (chou3)\r\n4E13 (zhuan1)\r\n4E14 (qie3,ju1)\r\n...\r\n```\r\n\r\n其中，对于没有或者找不到读音的汉字，统一标注为`none0`，我统计了一下，这样的汉字一共有525个。\r\n\r\n本着将字典文件尽可能减小体积的目标，发现上述文件中除了第一个〇(3007)之外，其它都是连续的，所以我把它改成了如下结构，文件体积也从`280kb`减小到了`117kb`：\r\n\r\n```javascript\r\nvar pinyin_dict_withtone = \"yi1,ding1 zheng1,kao3,qi1,shang4 shang3,xia4,none0,wan4 mo4,zhang4,san1,shang4 shang3,xia4,ji1,bu4 bu2 fou3,yu3 yu4 yu2,mian3,gai4,chou3,chou3,zhuan1,qie3 ju1...\";\r\n```\r\n\r\n该字典文件的缺点是声调是用数字标出的，如果想要得出类似`xiǎo míng tóng xué`这样的拼音的话，需要一个算法将合适位置的字母转换成`āáǎàōóǒòēéěèīíǐìūúǔùüǖǘǚǜńň`。\r\n\r\n本来还准备自己尝试写一个转换的方法的，后来又找到了如下[字典文件](http://zi.artx.cn/zi/)(下面称为字典B)，它收录了20867个汉字，也支持声调和多音字，但是声调是直接标在字母上方的，由于它将汉字也列举出来，所以文件体积比较大，有`327kb`，大致内容如下：\r\n\r\n```javascript\r\n{\r\n	\"吖\": \"yā,ā\",\r\n	\"阿\": \"ā,ē\",\r\n	\"呵\": \"hē,a,kē\",\r\n	\"嗄\": \"shà,á\",\r\n	\"啊\": \"ā,á,ǎ,à,a\",\r\n	\"腌\": \"ā,yān\",\r\n	\"锕\": \"ā\",\r\n	\"錒\": \"ā\",\r\n	\"矮\": \"ǎi\",\r\n	\"爱\": \"ài\",\r\n	\"挨\": \"āi,ái\",\r\n	\"哎\": \"āi\",\r\n	\"碍\": \"ài\",\r\n	\"癌\": \"ái\",\r\n	\"艾\": \"ài\",\r\n	\"唉\": \"āi,ài\",\r\n	\"蔼\": \"ǎi\"\r\n	/* 省略其它 */\r\n}\r\n```\r\n\r\n但是经过比对，发现有502个汉字是字典A中读音为`none`但是字典B中有读音的，还有21个汉字是字典A中有但是B中没有的：\r\n\r\n```javascript\r\n{\r\n	\"兙\": \"shí kè\",\r\n	\"兛\": \"qiān\",\r\n	\"兝\": \"fēn\",\r\n	\"兞\": \"máo\",\r\n	\"兡\": \"bǎi kè\",\r\n	\"兣\": \"lǐ\",\r\n	\"唞\": \"dǒu\",\r\n	\"嗧\": \"jiā lún\",\r\n	\"囍\": \"xǐ\",\r\n	\"堎\": \"lèng líng\",\r\n	\"猤\": \"hú\",\r\n	\"瓩\": \"qián wǎ\",\r\n	\"礽\": \"réng\",\r\n	\"膶\": \"rùn\",\r\n	\"芿\": \"rèng\",\r\n	\"蟘\": \"tè\",\r\n	\"貣\": \"tè\",\r\n	\"酿\": \"niàng niàn niáng\",\r\n	\"醸\": \"niàng\",\r\n	\"鋱\": \"tè\",\r\n	\"铽\": \"tè\"\r\n}\r\n```\r\n\r\n\r\n还有7个汉字是B中有但是A中没有的：\r\n\r\n```javascript\r\n{\r\n	\"㘄\": \"lēng\",\r\n	\"䉄\": \"léng\",\r\n	\"䬋\": \"léng\",\r\n	\"䮚\": \"lèng\",\r\n	\"䚏\": \"lèng,lì,lìn\",\r\n	\"㭁\": \"réng\",\r\n	\"䖆\": \"niàng\"\r\n}\r\n```\r\n\r\n所以我在字典A的基础上将二者进行了合并，得到了最终的字典文件 [pinyin_dict_withtone.js](https://github.com/liuxianan/pinyinjs/blob/master/pinyin_dict_withtone.js)，文件大小为`122kb`：\r\n\r\n```\r\nvar pinyin_dict_withtone = \"yī,dīng zhēng,kǎo qiǎo yú,qī,shàng,xià,hǎn,wàn mò,zhàng,sān,shàng shǎng,xià,qí jī...\";\r\n```\r\n\r\n\r\n# 如何使用\r\n\r\n我将这几种字典文件放在一起并简单封装了一下解析方法，使用中可以根据实际需要引入不同字典文件。\r\n\r\n封装好的3个方法：\r\n\r\n```javascript\r\n/**\r\n * 获取汉字的拼音首字母\r\n * @param str 汉字字符串，如果遇到非汉字则原样返回\r\n * @param polyphone 是否支持多音字，默认false，如果为true，会返回所有可能的组合数组\r\n */\r\npinyinUtil.getFirstLetter(str, polyphone);\r\n/**\r\n * 根据汉字获取拼音，如果不是汉字直接返回原字符\r\n * @param str 要转换的汉字\r\n * @param splitter 分隔字符，默认用空格分隔\r\n * @param withtone 返回结果是否包含声调，默认是\r\n * @param polyphone 是否支持多音字，默认否\r\n*/\r\npinyinUtil.getPinyin(str, splitter, withtone, polyphone);\r\n/**\r\n * 拼音转汉字，只支持单个汉字，返回所有匹配的汉字组合\r\n * @param pinyin 单个汉字的拼音，不能包含声调\r\n */\r\npinyinUtil.getHanzi(pinyin)；\r\n```\r\n\r\n下面分别针对不同场合如何使用作介绍。\r\n\r\n## 如果你只需要获取拼音首字母\r\n\r\n```javascript\r\n<script type=\"text/javascript\" src=\"pinyin_dict_firstletter.js\"></script>\r\n<script type=\"text/javascript\" src=\"pinyinUtil.js\"></script>\r\n\r\n<script type=\"text/javascript\">\r\npinyinUtil.getFirstLetter(\'小茗同学\'); // 输出 XMTX\r\npinyinUtil.getFirstLetter(\'大中国\', true); // 输出 [\'DZG\', \'TZG\']\r\n</script>\r\n```\r\n\r\n需要特别说明的是，如果你引入的是其它2个字典文件，也同样可以获取拼音首字母的，只是说用这个字典文件更适合。\r\n\r\n## 如果拼音不需要声调\r\n\r\n```javascript\r\n<script type=\"text/javascript\" src=\"pinyin_dict_notone.js\"></script>\r\n<script type=\"text/javascript\" src=\"pinyinUtil.js\"></script>\r\n\r\n<script type=\"text/javascript\">\r\npinyinUtil.getPinyin(\'小茗同学\'); // 输出 \'xiao ming tong xue\'\r\npinyinUtil.getHanzi(\'ming\'); // 输出 \'明名命鸣铭冥茗溟酩瞑螟暝\'\r\n</script>\r\n```\r\n\r\n## 如果需要声调或者需要处理生僻字\r\n\r\n```javascript\r\n<script type=\"text/javascript\" src=\"pinyin_dict_withtone.js\"></script>\r\n<script type=\"text/javascript\" src=\"pinyinUtil.js\"></script>\r\n\r\n<script type=\"text/javascript\">\r\npinyinUtil.getPinyin(\'小茗同学\'); // 输出 \'xiǎo míng tóng xué\'\r\npinyinUtil.getPinyin(\'小茗同学\', \'-\', true, true); // 输出 [\'xiǎo-míng-tóng-xué\', \'xiǎo-míng-tòng-xué\']\r\n</script>\r\n```\r\n\r\n## 如果需要精准识别多音字\r\n\r\n由于词典文件较大，本示例不推荐在web环境下使用：\r\n\r\n```javascript\r\n<script type=\"text/javascript\" src=\"dict/pinyin_dict_withtone.js\"></script>\r\n<script type=\"text/javascript\" src=\"dict/pinyin_dict_polyphone.js\"></script>\r\n<script type=\"text/javascript\" src=\"pinyinUtil.js\"></script>\r\n\r\n<script type=\"text/javascript\">\r\npinyinUtil.getPinyin(\'长城和长大\', \' \', true, true)； // 输出：cháng chéng hé zhǎng dà\r\npinyinUtil.getPinyin(\'喝水和喝彩\', \' \', true, true)； // 输出：hē shuǐ hé hè cǎi\r\npinyinUtil.getPinyin(\'伟大的大夫\', \' \', true, true)； // 输出：wěi dà de dài fū\r\n</script>\r\n```\r\n\r\n# 关于简单拼音输入法\r\n\r\n一个正式的输入法需要考虑的东西太多太多，比如词库、用户个人输入习惯等，这里只是实现一个最简单的输入法，没有任何词库（虽然加上也可以，但是web环境不适合引入太大的文件）。\r\n\r\n推荐使用第二个字典文件`pinyin_dict_notone.js`，虽然字典三字数更多，但是不能按照汉字使用频率排序，一些生僻字反而在前面。\r\n\r\n```html\r\n<link rel=\"stylesheet\" type=\"text/css\" href=\"simple-input-method/simple-input-method.css\">\r\n<input type=\"text\" class=\"test-input-method\"/>\r\n<script type=\"text/javascript\" src=\"pinyin_dict_notone.js\"></script>\r\n<script type=\"text/javascript\" src=\"pinyinUtil.js\"></script>\r\n<script type=\"text/javascript\" src=\"simple-input-method/simple-input-method.js\"></script>\r\n<script type=\"text/javascript\">\r\n	SimpleInputMethod.init(\'.test-input-method\');\r\n</script>\r\n```\r\n\r\n# 结语\r\n\r\n由于本工具类的目标环境是web，而web注定了文件体积不能太大，所以不能引入太大的词库文件，由于没有词库的支持，所以多音字无法识别，实现的拼音输入法也无法智能地匹配出合适的词语，需要词库支持的可以参考这个nodejs环境下的项目：https://github.com/hotoo/pinyin\r\n');
INSERT INTO `blog` VALUES ('3', '祭严青华文(2008年4月28日晚泪笔)', '杂七杂八', 'http://blog.liuxianan.com/ji-yanqinghua-wen.html', '# 说明\r\n\r\n高中时无聊写的一篇超级无聊“文言文”。\r\n\r\n![](http://image.liuxianan.com/201608/20160831_160957_502_1351.jpg)\r\n\r\n# 正文\r\n\r\n(注：夫严青华者,乃吾一高中同学也。)\r\n\r\n呜呼！窃思君临繁世，仅十有六年耳。而今乃匆匆离父而去，为父不胜悲哉！犹记汝襁褓之时谓吾曰：“吾欲进清华大学堂！”念君小小年纪，即有如此凌云之志，不可渺之，遂名汝“青华”，盖激汝之志耳。\r\n\r\n及长，汝甚背父志，遂谴汝归学，然汝一而迟，再而旷，三而逃，日益猖獗，为父于斯亦无可奈何，遂听汝恣行。然汝何以短命至此耶？盖一“淫”字耳！是实乃为父之过也。汝尚值年少之时，吾视汝甚色，而听之任之，乃致汝有今日。\r\n\r\n去岁闻尔同学刘洋洋者曰，汝堕入情网，坠入爱河，不能自拔，需为父伸足助之，吾闻之甚悔。及尔归，力劝之。然孰料汝已无可救药，定丧于斯。而后汝复走庆路，命终于淫。呜呼哀哉！而今吾十六年之心血付诸东流矣！汝既无兄弟，终鲜慈母，令为父孤寂至此。而为父亦无续室之意，汝欲令为父独终余生乎？汝之不孝，实乃过之耳。汝正值青春年少之际，而为父适发白齿落之年，何以暮者存而少者亡乎？汝少时尝谓吾曰：“吾立志十有六岁即令汝抱孙！”汝今虽适之，然阴阳两隔，孙何以抱之！吾命中注定断后矣。汝若有心，当托梦于吾曰：“吾尚有遗腹之子于世，父亲大人无须忧之。”然吾亦知是实乃天方夜潭耳！何以言之？盖大夫尝言于吾曰：“汝儿高度不育，乃单倍体也。”\r\n\r\n是岁四月，吾置汝尸于旷野天葬。因汝未及加冠，不宜以死丧之礼葬之，遂有此举，望汝勿怪为父。呜呼！汝既殁，为父亦无法，唯清苦以度余生，孤独相随至老。汝至阴间，为父当便卖臭鞋，得铜板一二，为汝焚烧冥钱。汝亦当早日投胎，勿令为父牵挂。父子一场，情深难断。呜呼哀哉！尚飨！\r\n\r\n# 注解\r\n\r\n1. 襁褓之时：代指婴幼儿时期。\r\n2. 续室：意指续娶。\r\n3. 单倍体：不懂的看高二的生物书去！\r\n4. 天葬：藏民处理遗体的一种方法，把尸体运到葬场或旷野，让鹰、乌鸦等鸟类吃掉。\r\n5. 加冠：指二十岁。\r\n6. 复走庆路：“庆”，指西门庆。');
