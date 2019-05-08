# **Kaspresso**

## **Espresso**
Для UI-тестирования существует немало различных фреймворков. Когда-то наиболее популярным был фреймворк **Robotium**, но
сейчас его вытеснил **Espresso**, который мы и будем рассматривать. Оба этих тестовых фреймворка позволяют выполнять
действия над **UI**-элементами (кнопками, полями ввода и другими) и проверять различные условия для этих **UI**-элементов
(корректность текста, visibility, enabled / disabled и другие). Кроме того, есть и другие фреймворки, например
UiAutomator, который позволяет проверять работу приложения с точки зрения **UI** на очень высоком уровне взаимодействия.

**Espresso** это тестовый фреймворк от **Google**, он включен в пакет **android.supprot.test**. По факту, на данный момент в
индустрии он является стандартным инструментом для реализации **UI**-тестирования. В базовом представлении фреймворк
**Espresso** позволяет находить **View** на экране, выполнять с ними какие-то действия и проверять выполнение условий.
Простейший тест на **Espresso** может выглядеть следующим образом:

    @RunWith(AndroidJUnit4::class)
    class MainActivityTest {
        @Rule
        @JvmField
        val rule = ActivityTestRule(MainActivity::class.java)
    
        @Test
        fun testTypeLogin() {
            onView(withId(R.id.login_input_ev))
                .perform(typeText("MyLogin"))
                .check(matches(withText("MyLogin")))
        }
    }

Мы указываем в качестве Runner класс **AndroidJUnit4**, далее мы задаем правило **Rule**, которое указывает, какую **Activity**
запустить для тестов. И основное – в тестовом методе мы находим **View** с **id** **login_input_ev** и над ней выполняем действие
ввода текста MyLogin, а затем проверяем, что введенный текст действительно появился в поле ввода с **id** **login_input_ev**.

Это и есть общая схема всех тестов с **Espresso**:
1. Найти **View**, передав в метод **onView** объект **Matcher**.
2. Выполнить какие-то действия над этой **View**, передав в метод perform объект **ViewAction**.
3. Проверить состояние **View**, передав в метод **check** объект **ViewAssertion**. Обычно для создания объекта **ViewAssertion**
   используют метод **matches**, который принимает объект **Matcher** из пункта 1.

Для каждого из этих объектов существует большое количество стандартных методов, которые позволяют покрыть подавляющее большинство сценариев проверки:
- **withId, withText, withHint, withTagKey**, … – **Matcher**.
- **click, doubleClick, scrollTo, swipeLeft, typeText**, … – **ViewAction**.
- **matches, doesNotExist, isLeftOf, noMultilineButtons** – **ViewAssertion**.

И, разумеется, если вам не хватит стандартных объектов, вы всегда можете создать свои собственные **matcher**'ы,
**viewaction**'ы и **viewassertion**'ы.

## **Kakao**
Но, как и все в нашем мире, **Espresso** тоже не идеален, он обладает рядом недостатков. **Espresso** реализован с
использованием фреймворка **Hamcrest**, а значит его синтаксис построен на иерархическом дереве матчеров. И при довольно
глубокой вложенности матчеров код становится плохочитаемым:

    @Test
    fun espressoTest() {
        onView(allOf(allOf(withId(R.id.kakao),
            isDescendantOfA(withId(R.id.coffee_varieties))),
            isDescendantOfA(withId(R.id.contentView))))
            .check(matches(withEffectiveVisibility(View.VISIBLE)))
    }

Для решения этой проблемы существует уже готовое решение - **Kakao** - простой **Kotlin DSL** для **Android** **UI** тестов с **Espresso**.
С его помощью такая функция свернется в легко читаемую:

    @Test
    fun espressoTest() {
        screen {
            kakao { isVisible() }
        }
    }

Двумя основными концепциями **Какао** являются:

**Screen.** Это некоторая интерпретация паттерна **PageObject**, пришедшего к нам из мира web разработки. Это класс,
который создается для каждого тестируемого экрана приложения, в нем мы объявляем некоторые представления наших
**views** с этого экрана, с которыми мы будем взаимодействовать во время теста.

    class MainActivityScreen : Screen<MainActivityScreen>() {
        val content: KView = KView { withId(R.id.content) }
        val button: KButton = KButton { withId(R.id.button) }
        val textView: KTextView = KTextView { withId(R.id.text_view) }
    }


**KViews.** Это, собственно, и есть представления наших **views**, которые объявляются внутри **screen**'ов. Библиотека
предоставляет их в большом количестве (**KTextView, KEditText, KButton, KRecyclerView, KWebView**), но мы также можем
объявлять свои, что достаточно просто, ведь все **KViews** это просто пустые классы, которые наследуют логику
интерфейсов: действий (**Actions**) и утверждений (**Assertions**). (Такую крутую возможность нам предоставляет **Kotlin** -
писать реализацию в интерфейсах. Это позволяет добиться потрясающей гибкости при плоской иерархической структуре
наших классов).

    class KEditText : KBaseView<KEditText>, EditableActions, EditableAssertions {
        constructor(function: ViewBuilder.() -> Unit) : super(function)
        …
    }

После декларации иерархии пользовательского интерфейса, вы можете получить прямой доступ к объекту **Screen** и всем
находящимся в нем объектам **KView** при помощи оператора **invoke** и совершать различные действия или утверждения в каскадном
стиле. Таким образом наш тест превращается в нечто такое:

    @RunWith(AndroidJUnit4::class)
    class MainActivityTest {
        @Rule
        @JvmField
        val rule = ActivityTestRule(MainActivity::class.java)

        val screen = MainActivityScreen()

        @Test
        fun test() {
            screen {
                content { isVisible() }

                textView {
                    isVisible()
                    hasAnyText()
                    click()
                }

                button {
                    hasText("BUTTON")
                    click()
                }
            }
        } 
    }

## **Недостатки** **Espresso**
Несмотря на то, что **Espresso** берет на себя всю работу по взаимодействию с интерфейсом, он недостаточен для решения всех
задач, возникающих при покрытии приложения **UI**-тестами, а также при автоматизации запуска этих тестов:
1. **Espresso** недостаточно стабилен при асинхронном поведении интерфейса и при работе со списками. (При работе с сетью
   или просто в ситуациях, когда вьюхи на экране появляются не моментально, **Espresso** может вести себя ненадежно - а
   именно, может не дождаться появления нужной вьюхи и завалить тест. А также, если вью видна не полностью или до
   нее нужно доскроллить, **Espresso** так же может такую вью не найти и свалиться с **NoMatchingViewException**).
2. Не предоставляет инструмента логирования действий, происходящих на экране. (Когда тест падает, **Espresso**
   выбрасывает исключение с описанием причины падения, например, в иерархии вью нашлось несколько элементов,
   подходящих под указанные матчеры, однако мы в таком случае не имеем на руках записанной последовательности
   действий, которая привела к падению, что сильно усложняет процесс поиска причины ошибки. А данный факт является
   особо значимым, когда мы говорим именно об автотестировании).
3. А еще он, естественно, не предоставляет инструмента снятия скриншотов, который отлично бы дополнил текстовое
   логгирование производимых действий. (Логи + скриншоты позволят делать практически исчерпывающий отчет о
   прохождении тесткейса, в котором в случае ошибки содержалась бы последовательность действий, приведшая к падению
   теста, а также непосредственно скрин экрана с ошибкой).
4. Не предоставляет инструмента для взаимодействия с операционной системой за пределами экранов нашего приложения.
   (Например, в тестовых сценариях зачастую необходимо выдать пермишены, включить/отключить интернет,
   включить/отключить аксессибилити, установить/удалить приложение, открыть ссылку в браузере или просто-напросто
   сменить ориентацию девайса).
5. Не предоставляет решения по архитектуре кода на уровне тестовых сценариев. (Но когда в проекте, а еще хуже - в
   нескольких проектах - появляется солидная пачка тестовых сценариев, рождается необходимость выделения архитектуры
   кода для тестирования, а также потребность в его переиспользовании. Более того, в таком случае желательно наличие
   фреймворка, который строго бы диктовал пользователю порядок и структуру реализуемого тестового сценария.)


**Kakao** же, несмотря, на свои так же очевидные плюсы, абсолютно не покрывает описанных потребностей в силу того, что он
реализован исключительно как **DSL**-обертка над **Espresso** (что тоже необходимо).

Конечно, с такой проблемой мы столкнулись не первые, но тем не менее полноценного готового программного решения, которое
мы могли бы просто взять и использовать, на данный момент в открытом доступе не существует. И есть основания полагать,
что не только в открытом. Ситуация в индустрии на данный момент такова, что одни из лидеров в сфере автоматизации
мобильного **UI**-тестирования компания **Avito** на самом деле используют в своей практике некий собственный фреймворк, который
решает уже описанные выше задачи. Он выложен в открытый доступ на **github**, любой желающий может с ним ознакомиться.
Однако этот инструмент писался под их собственные нужды, специфика которых только частично пересекается с нуждами
**Лаборатории Касперского**, а также он не является чем-то законченным, а существует скорее в виде более-менее
структурированного набора инструментов. К тому же сами авторы этого фреймворка заявляют, что скорее всего он может быть
полезен вне **Avito** именно как совокупность идей и наработок, из которой полезно будет что-то почерпнуть и позаимствовать.
Чем мы, конечно же, и воспользовались. И еще они не используют **Какао**. (Потому что писали его раньше, чем **Какао** был
выложен в опенсорс).

Таким образом, осознание этих потребностей и отсутствие полноценных готовых решений, которые бы их покрывали,
подталкивают нас к необходимости реализации собственного инструмента для **UI**-тестирования, работающего на базе **Espresso**,
**Kakao** и не только. Итак, встречайте **Kaspresso**.

## **Kaspresso**
**Kaspresso** - это фреймворк для организации кода **UI**-тестов на уровне тестовых сценариев с использованием **Espresso**, **Kakao**,
**UiAutomator**, **Spoon**, а также с использованием идей из **avito-tech/android-ui-testing**. Он может быть гибко настроен с
помощью конфигуратора настроек (в котором можно регулировать, включать и отключать различные опции), он реализует так
называемую **flaky safety** (то есть обеспечивает стабильность взаимодействия с элементами интерфейса), предоставляет гибкий
и удобный механизм логирования тестовых шагов и самих действий и утверждений над элементами интерфейса, механизм снятия
скриншотов во время выплонения теста и при его падении, а также предоставляет единый интерфейс к различным инструментам
для работы с операционной системой и девайсом за пределами тестируемого приложения. И, что не менее важно, Kaspresso
может быть легко расширен (и будет, уже в ближайшем будущем) благодаря механизму интерсепторов. Теперь обо всем этом по
порядку и поподробнее.

### **Configurator.Builder**
Ваша работа с **Kaspresso** как пользователя начинается с реализации вашего тестового сценария, в котором вы будете
описывать ваш **ActivityTestRule** и ваши тестовые методы с аннотацией **@Test**, в которых и будут выполняться шаги тесткейса.
Для этого просто-напросто необходимо создать наследника класса **TestCase**, и при вызове конструктора этого базового класса
**TestCase** вы можете указать объект **Configurator.Builder** c нужными вам настройками. При создании объекта базового класса
**TestCase** билдер будет применен, и в **object Configurator** (синглтон в **Kotlin**) попадут указанные вами настройки. Если вы
вызвали конструктор базового класса **TestCase** без параметров, будут применены настройки по-умолчанию. Почему так? Так как
запуском тестов, а в нашем случае - наследника класса **TestCase** - управляет внешняя сущность **testrunner**, то для нашего
клиентского кода точкой входа служит как раз инициализация базового класса **TestCase**, и именно в этот момент
инициализируется синглтон **Configurator** и в него проставляются настройки. Таким образом мы гарантируем, что **Configurator**
перед началом наших тестов уже готов и настроен. Более того, мы гарантируем, что настройки будут проставлены только один
раз в самом начале и больше в течение теста меняться не будут, так как доступ к терминальному методу билдера,
применяющего настройки к **Configurator**, есть только внутри пакета фреймворка, и вызывается он только самим фреймворком
при создании базового класса **TestCase**.

    @RunWith(AndroidJUnit4::class)
    class MyTestCase : TestCase(
        Configurator.Builder().apply {
            attemptsTimeoutMs = TimeUnit.SECONDS.toMillis(5)
            viewActionInterceptors = listOf(LoggingViewActionInterceptor(logger))
            viewAssertionInterceptors = listOf(LoggingViewAssertionInterceptor(logger))
            executingInterceptor = FlakySafeExecutingInterceptor()
            failureInterceptor = LoggingFailureInterceptor(logger)
        }
    ) {
        @Rule
        @JvmField
        val mActivityRule = ActivityTestRule(MainActivity::class.java)

        @Test
        fun test () { … }
    }

### **Configurator**
Так что же это за настройки и что лежит внутри **Configurator**? В Конфигураторе вы можете настроить различные интерсепторы
(объекты-перехватчики для внедрения дополнительных действий при каждом вызове перехватываемой функции) для всех видов
взаимодействия с интерфейсом:

    1. List<ViewActionInterceptor>
    2. List<ViewAssertionInterceptor>
    3. List<AtomInterceptor>
    4. List<WebAssertionInterceptor>
    5. FailureInterceptor
    6. ExecutingInterceptor

#### **Interceptors**
Первые 4 вида интерсепторов указываются списком, так как их может быть множество, и все они будут вызываться перед самим
вызовом перехватываемой функции (например, в случае **ViewActionInterceptor** это вызов **ViewAction#perform**) по очереди.
Поэтому эти интерсепторы никак не влияют и не изменяют поведение перехватываемой функции - только совершают
дополнительные действия до ее вызова. Например, **LoggingViewActionInterceptor** просто пишет в лог информацию о каждом
вызове **ViewAction#perform** - какое действие было совершено и на каком элементе интерфейса.

Вызов таких интерсепторов осуществляется следующим образом: вместо обычного **ViewAction** мы работаем с оберткой -
**ViewActionProxy**, который содержит в себе этот самый объект **ViewAction**, и в то же время реализует его интерфейс, поэтому
при вызове **ViewActionProxy#perform**, прокси вызывает сначала соответствующие интерсепторы, а уже затем производит вызов
непосредственно **ViewAction#perform**. Для того чтобы подменить прямой вызов **ViewAction#perform** на вызов
**ViewActionProxy#perform**, мы внесли изменения в Какао и расширили его возможности по кастомизации. Если вы используете
настройки **Configurator** по умолчанию, то будут использоваться логгирующие интерсепторы для всех 4 видов взаимодействия.

    class ViewActionProxy(
        private val viewAction: ViewAction,
        private val interceptors: List<ViewActionInterceptor>
    ) : ViewAction {

        override fun perform(uiController: UiController, view: View) {
            interceptors.forEach { it.intercept(viewAction, view) }
            viewAction.perform(uiController, view)
        }

        …
    }

#### **FailureInterceptor**
Далее, как понятно из названия, **FailureInterceptor** служит для перехвата и обработки ошибок. Если вы укажете реализацию
**FailureInterceptor** в **Configurator**, то этот класс будет использоваться в качестве обработчика ошибок по умолчанию. При
инициализации **Configurator**, он будет передан **Espresso** и будет вызываться при каждой ошибке при любом виде
взаимодействия с элементами интерфейса. Вы также можете на вашем конкретном элементе интерфейса явно вызвать метод
**withFailureHandler** и положить в него в качестве параметра вашу собственную реализацию класса **FailureHandler**, в таком
случае в качестве **failure handler** конкретно для этого вью будет использоваться он, а не дефолтный. Если вы используете
настройки **Configurator** по умолчанию, в качестве реализации **FailureInterceptor** будет использоваться
**LoggingFailureInterceptor**, который просто пишет в лог читаемое описание ошибки и выводит стек-трейс, прежде чем просто
выбросить исключение. Так как **Espresso** поддерживает только один дефолтный **FailureHandler**, на данный момент
**FailureInterceptor** может быть установлен также только один, однако при возникновении необходимости в будущем на уровне
нашего фреймворка можно будет эмулировать несколько уровней обработки ошибок посредством каскадного вызова нескольких
**FailureInterceptor** в рамках одного объекта **FailureHandler** от **Espresso**.

#### **ExecutingInterceptor**
**ExecutingInterceptor** уже ответственен за непосредственный вызов **ViewActionProxy#perform**, в частности, его реализация
**FlakySafeExecutingInterceptor** отвечает за обеспечение **flaky safety** посредством оборачивания вызова
**ViewActionProxy#perform** методом **attempt**, который в течение определенного периода времени с определенной частотой
пытается произвести взаимодействие с интерфейсом. Если при попытке взаимодействия мы валимся с исключением, метод
**attempt** не позволяет тесту упасть и через определенный промежуток времени вновь пытается произвести то же самое
взаимодействие. *Таймаут*, в течение которого производятся попытки, их *частота*, а также так называемые *"разрешенные"
исключения*, поймав которые, метод **attempt** не пробрасывает их выше, а пытается произвести взаимодействие снова,
настраиваются в нашем **Configurator**. Таким образом, указав в качестве **ExecutingInterceptor** в **Configurator** класс
**FlakySafeExecutingInterceptor**, выставив таймаут аттемптов в *2 секунды*, и период между аттемптами в *500 мс*, а также
добавив в разрешенные исключения **PerformException** и **NoMatchingViewException**, фреймворк каждое взаимодействие с
элементами интерфейса будет пытаться произвести в течение *2 секунд*, за которые будет выполнено *4 попытки*, если, конечно,
взаимодействие не было произведено успешно сразу с первой попытке (в таком случае никакого ожидания выполняться не
будет, а следовательно при нормальном течении теста время его выполнения увеличиваться не будет). Получив при попытке
произвести взаимодействие, например, **NoMatchingViewException**, фреймворк будет пробовать провзаимодействовать снова, так
как необходимого элемента интерфейса на экране он пока не нашел. Однако, если выскачет, например **RuntimeException**, тест
все таки упадет, так как мы не ожидали получить это исключение, такое исключение уже говорит о том, что сломалось что-то
другое. Таким образом повышается устойчивость и надежность процесса выполнения тестов. Также, вы можете обернуть в метод
**attempt** вызов любого количества любых методов **KView**. Например, когда во время теста приложение пытается
авторизироваться, ждет ответа от сервера, и показывает крутилку, мы в течение, например, *10 секунд* можем пытаться
дождаться появления кнопки ОК и затем нажать на нее. С голым **Espresso** или **Kakao** это было бы невозможно.

    class FlakySafeExecutingInterceptor : ExecutingInterceptor {
        override fun interceptAndExecute(
            function: () -> ViewInteraction
        ) : ViewInteraction {
            attempt(
                timeoutMs = Configurator.attemptsTimeoutMs,
                intervalMs = Configurator.attemptsIntervalMs,
                logger = Configurator.logger,
                allowedExceptions = Configurator.allowedExceptionsForAttempt
            ) {
                function.invoke()
            }
    }

#### **Loggers**
В настройках **Configurator** вы можете установить свои логгеры, которые будут использоваться для записи в нужный вам
поток вывода, для этого нужно указать реализации интерфейса **UiTestLogger** в соответствующих полях. Логгера два - внутренний
и внешний. Внутренний используется самим фреймворком и из клиентского кода доступа к нему нет, для этого можно
использовать второй внешний логгер. Вы указываете его в **Configurator**, а затем обращаетесь к нему из кода просто как
синглтону **KLogger**. Разница между ними в том, что они пишут с различными тегами, сделано это для избежания путаницы в
выходных лог-файлах.

#### **Device and system managers**
Как уже упоминалось, фреймворк содержит единый общий интерфейс для взаимодействия с девайсом и операционной системой вне
экранов тестируемого приложения. Для этого существует фасадный синглтон **Device**, через который вы можете обратиться к
различным менеджерам:
1. **Device.apps** (для работы с **installer, launcher, package manager**, позволяет устанавливать, удалять,
   запускать, убивать процесс приложения, открывать ссылки в браузере и тд)
2. **Device.activites** (для получения, например, запущенной активити)
3. **Device.files** (для загрузки файлов на девайс)
4. **Device.internet** (позволяет включать/выключать соединение с интернетом или вайфай на девайсе)
5. **Device.screenshots** (позволяет снимать скриншоты, примем, как внутри тестируемого приложения, так и вне его)
6. **Device.accessibility** (позволяет включать/отключать аксессибилити)
7. **Device.permissions** (позволяет выдвать или запрещать пермишены)
8. **Device.exploit** (который позволяет эмулировать физическое взаимодействие с девайсом, например, нажимать на кнопки
   назад или домой, менять ориентацию девайса)

Дефолтные рабочие реализации интерфейсов этих менеджеров автоматически проставляются в **Configurator**, и обращаться к ним
из клиентского кода вы можете как раз через **Device**. Но если вы захотите использовать свою реализацию какого-то из
менедждеров, вы можете указать ее в **Configurator**, и она также будет доступна вам при обращении через **Device** к
соответствующему реализуемому интерфейсу.

    interface Apps {
        fun install(apkPath: String)
        fun uninstall(packageName: String)
        fun openUrlInChrome(url: String)
        fun launch(packageName: String, data: Uri? = null)
        fun openRecent(contentDescription: String)
        fun kill(packageName: String = targetAppPackageName)
    }

### **AdbServer**
Для работы некоторых методов некоторых менеджеров требуется выполнение **adb-команд** (например при установке/удалении
приложения, остановке стороннего приложения  у apps, при инъекции системных настроек (выдача **accessibility**,
включение/выключение **WiFi**  у **internet**, поворот экрана у **exploit**)). Так как тестируемый девайс сам не может выполнять
адб команды, это должен делать кто-то внешний. Для выполнения этих команд был реализован специальный адб-сервер.
Чтобы все запустить:
##### **На стороне хоста**
Необходимо запустить клиент до выполнения тестов. Именно этот клиент будет исполнять adb-команды. Он представляет из себя jar-файл  **HostConnectionClient.jar**
Перед стартом тестов необходимо стартовать его, выполнив команду
```
java -jar HostConnectionClient.jar
```
Данный джарник в уже собранном виде можно достать в проекте [Android.Autotests.SupportFiles](https://hqrndtfs.avp.ru/tfs/DefaultCollection/MobileProducts/_git/Android.Autotests.SupportFiles), 
а конкретнее, например в проекте KISA, лежит [здесь](https://hqrndtfs.avp.ru/tfs/DefaultCollection/MobileProducts/_git/Android.Autotests.SupportFiles?path=%2FHostConnectionClient.jar&version=GBkisa). <br>
**Берите HostConnectionClient.jar из src/main/assets**
##### **На стороне девайса**
Для исполнения команд необходимо воспользоваться классом:
```kotlin
object AdbServer {
    fun performCmd(vararg commands: String) { … }
    fun performAdb(vararg commands: String) { … }
    fun performShell(vararg commands: String) { … }
}
```
Пример использования 1: Установка стороннего apk
```kotlin
fun install(apkPath: String) {
    AdbServer.performAdb("install $apkPath")
}
```
Пример использования 2: Отключение интернета на устройстве
```kotlin
fun disable() {
    AdbServer.performAdb("shell svc data disable", "shell svc wifi disable")
}
```
> **Внимание!** Будьте осторожны при использовании **performCmd** - исполняемые вами команды должны поддерживаться всеми возможными хост-машинами. Перед использованием лучше посоветуйтесь с вашей командой: возможно существует решение вашей задачи без использования **performCmd**

### **TestCase**
Вернемся к нашему **TestCase**: вот мы создали своего наследника класса **TestCase**, настроили **Configurator.Builder**, передали
его в конструктор базового класса **TestCase**, описали где-то **KScreen**'ы с элементами интерфейса для нашего теста, теперь
реализуем непосредственно сами тестовые методы. Внутри метода с аннотацией **@Test** мы вызываем метод базового класса
**beforeTest**, который в качестве аргумента принимает лямбда выражение с действиями, которые будут выполнены
непосредственно перед сами тестом. Это некоторый аналог метода с аннотацией **@Before**, однако метод **@Before** будет
вызываться перед каждым методом **@Test**, а нам может понадобиться для каждого метода **@Test** выполнять свои предварительные
действия. Например, если наш тесткейс начинается с **MainActivity**, то проход визарда или авторизация могут быть описаны
как раз в методе **beforeTest** для приведения состояния приложения к состоянию, необходимого для тесткейса. Далее
вызывается метод **afterTest**, который, собственно, делает то же самое, только наоборот - он используется для возвращения
состояния приложения к исходному, если это необходимо. Например, если мы, в **beforeTest** отключали интернет, то в методе
**afterTest** нам необходимо его включить обратно. (При запуске тесткейса, приложение открывается заново, возвращаться с
экрана на **MainActivityScreen** необходимости нет, однако, если меняли состояние системы, его нужно будет вернуть в исходное). И,
наконец, после этого мы можем вызвать метод **runSteps**, который в качестве аргумента принимает лямбда-выражение, внутри
которого должны лежать последовательные вызовы функции **step**. Функция **step** это непосредственно представление шага
тесткейса, эта функция принимает параметром строковое описание шага и *лямбда-выражение*, в котором описываются уже
конкретные действия данного шага. Кроме запуска *лямбда-выражения*, она делает еще много всего интересного, пишет в лог
описание данного шага для хорошей читаемости лог-файла, а также делает скриншот состояния экрана на конец шага, либо,
если произошла ошибка, делается скриншот экрана прямо перед тем, как пробросить пойманное исключение дальше. Таким
образом в лог-файле будет легко разобраться, если что-то пошло не так: в каком тесткейсе произошла ошибка, какие
действия совершались переде этим (это пишут логгирующие интерсепторы), задействовался ли механизм аттемптов, и если да,
то сколько было произведено попыток и в течение какого времени, а также можно будет посмотреть скриншоты успешно
выполненных шагов и финальный скриншот с ошибкой.

    @Test
    fun test() {
        beforeTest {
            activityTestRule.launchActivity(null)
            Device.exploit.setOrientation(Orientation.Landscape)
        }.afterTest {
            Device.exploit.setOrientation(Orientation.Portrait)
        }.runSteps {
            step("Ввести логин") {
                signInScreen {
                    emailEditText.typeText("my.email@gmail.com")
                }
            }
            step("Свернуть и развернуть приложение, убедиться, что логин отображается") {
                Device.exploit.pressHome()
                Device.apps.openRecent("MyApp")

                signInScreen {
                    emailEditText.hasText("my.email@gmail.com")
                }
            }
        }
    }

### **SubCase**
Также при написании тесткейсов будут встречаться определенные часто повторяющиеся фиксированные последовательности
шагов, которые будут выполняться в множестве тесткейсов, например, проход визарда. Такие последовательности можно
вынести в отдельный класс-наследник класса **SubCase**, а из из кода тесткейса они смогут вызываться одной строчкой
**ExampleSubCase().run()**. Однако, увлекаться вынесением шагов в такие сабкейсы все же не стоит, так как это потенциально
может усложнить восприятие тесткейса.

    class EnterLoginSubCase(private val login: String): SubCase() {
        override val steps: Scenario.() -> Unit = {
            step("Ввести логин") {
                signInScreen {
                    emailEditText.typeText(login)
                }
            }
        }
    }

    @Test
    fun test() {
        beforeTest {
            activityTestRule.launchActivity(null)
            Device.exploit.setOrientation(Orientation.Landscape)
        }.afterTest {
            Device.exploit.setOrientation(Orientation.Portrait)
        }.runSteps {
            EnterLoginSubCase("my.email@gmail.com").run()

            step("Свернуть и развернуть приложение, убедиться, что логин отображается") {
                Device.exploit.pressHome()
                Device.apps.openRecent("MyApp")

                signInScreen {
                    emailEditText.hasText("my.email@gmail.com")
                }
            }
        }
    }

## DocLocScreenshotTestCase

Локализаторам нужны скрины с мета-информацией по фичам с различными локализациями. 
Для создания таких скринов нужно написать специальный тест, наследуемый от ```DocLocScreenshotTestCase```
В конструктор передается относительный путь для сохранения скринов и соответствующей мета-информацией. 
Когда нужно сделать снимок, вызывается метод ```captureScreenshot(name)```, который сделает снимок и сохранит его в ```<locale>/<переданная директория для скринов>/name```.
Пееред запуском теста фреймворк автоматически очистит директорию для снимков.
Пример теста можно найти в документации к ```DocLocScreenshotTestCase```.


## **Let's start**
Подключить **Kaspresso** себе в проект вы можете с помощью:
    
    androidTestImplementation "com.kaspersky.components:kaspresso:$versions.kaspresso"
    testCompileOnly "com.kaspersky.components:klkakao:$versions.klkakao"