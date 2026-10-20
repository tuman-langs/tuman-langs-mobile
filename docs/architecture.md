# Архитектура приложения

Этот документ отвечает на вопрос «куда положить файл». Его читают участники и Claude Code перед каждой задачей. За основу взята архитектура CalorAI Mobile: Clean Architecture, деление на `core` и `features`, MVVM с состоянием экрана. Мы её упростили под MVP и команду первокурсников.

Если нужного места нет или правило мешает, не изобретайте своё. Напишите комментарий в задаче, руководитель решит и поправит этот документ.

## 1. Модули Gradle

```
Tuman/                       корень проекта Android Studio
├── app/                     приложение: экраны, база, сеть, напоминания
├── core-srs/                алгоритм повторения FSRS: чистый Kotlin, без Android
├── gradle/libs.versions.toml  версии всех библиотек, в одном месте
├── docs/                    документация (этот файл, ТЗ, команда)
├── .claude/                 общие настройки и команды Claude Code
└── .github/                 шаблоны задач и PR
```

| Модуль | Что внутри | От кого зависит | Кто ведёт |
|---|---|---|---|
| `:app` | всё, что видит пользователь, и хранение данных | `:core-srs` | Карпенко А., Бондаренко Е., Дворак М. |
| `:core-srs` | расчёт даты следующего показа, список «на сегодня» | ни от кого | Дворак М. |

`core-srs` не знает ни про Android, ни про Room, ни про экраны. Так выполняется требование ТЗ «алгоритм — отдельный модуль, независимый от UI», и его можно проверить обычными юнит-тестами без эмулятора.

## 2. Пакеты внутри `:app`

Корневой пакет — `ru.itmo.tuman`.

```
app/src/main/java/ru/itmo/tuman/
├── TumanApp.kt              класс Application: запуск Koin, канал уведомлений
├── MainActivity.kt          единственная Activity, внутри — навигация Compose
├── di/
│   └── AppModule.kt         собирает модули Koin из core и features
│
├── core/                    общее для всех экранов
│   ├── database/            Room: TumanDatabase, Entity, DAO, конвертеры     — Бондаренко
│   ├── network/             клиент сервера каталога (Retrofit), ошибки сети  — Карпенко
│   ├── navigation/          маршруты (Routes.kt) и TumanNavHost.kt           — Карпенко
│   ├── reminders/           WorkManager: ReminderWorker, планировщик         — Бондаренко
│   ├── uikit/               тема и общие компоненты по UI-kit из Figma       — Карпенко
│   │   ├── theme/           цвета, шрифты, TumanTheme
│   │   └── bashkirKeyboard/ панель букв ә ө ү ғ ҡ ң ҙ ҫ һ
│   └── utils/               мелкие помощники (даты, форматирование)
│
└── features/                один пакет на функцию приложения
    ├── home/                главный экран                                     — Карпенко
    ├── decks/               наборы и карточки: список, набор, редактор        — Карпенко
    ├── catalog/             каталог с сервера и загрузка набора               — Карпенко
    ├── review/              сессия повторения (использует core-srs)           — Дворак
    ├── progress/            прогресс: серия, XP, достижения                   — экран Дворак, расчёты Бондаренко
    └── settings/            дневная цель, время напоминаний                   — экран Дворак, напоминания Бондаренко
```

```
core-srs/src/main/kotlin/ru/itmo/tuman/srs/
├── CardState.kt             стабильность, сложность, дата показа, последний ответ
├── Rating.kt                AGAIN / HARD / GOOD / EASY = «не помню» / «трудно» / «хорошо» / «легко»
├── Scheduler.kt             fun review(state, rating, now): CardState
└── DueCards.kt              отбор карточек «на сегодня»
core-srs/src/test/kotlin/ru/itmo/tuman/srs/   юнит-тесты алгоритма
```

## 3. Как устроена одна функция (feature)

У каждой папки в `features/` одинаковая структура из трёх слоёв, как в CalorAI:

```
features/decks/
├── data/                    откуда берутся данные
│   ├── DecksRepositoryImpl.kt   работает с DAO из core/database
│   └── DeckMapper.kt            Entity ⇄ модель domain
├── domain/                  правила приложения, без Android
│   ├── model/Deck.kt, Card.kt
│   ├── DecksRepository.kt       интерфейс: что умеет хранилище
│   └── usecase/                 только если логика не помещается в одну строку
├── ui/                      то, что видит пользователь
│   ├── list/DecksScreen.kt, DecksViewModel.kt, DecksUiState.kt
│   └── edit/CardEditScreen.kt, CardEditViewModel.kt, CardEditUiState.kt
└── di/DecksModule.kt        Koin: какие классы создавать для этой функции
```

**Поток данных:** экран → `ViewModel` → `Repository` (интерфейс из `domain`) → реализация в `data` → DAO или сеть. Ответ идёт обратно как `UiState`, экран только рисует его.

```
DecksScreen ──действие──▶ DecksViewModel ──▶ DecksRepository ──▶ DecksRepositoryImpl ──▶ DeckDao (Room)
     ▲                         │
     └──── StateFlow<DecksUiState> ◀─────────────────────────────────────────────────┘
```

### Слои простыми словами

| Слой | Что можно | Чего нельзя |
|---|---|---|
| `ui` | Compose-экраны, ViewModel, `UiState`, обработка нажатий | обращаться к DAO и сети напрямую |
| `domain` | модели, интерфейсы репозиториев, use case | импортировать Android, Room, Retrofit, Compose |
| `data` | реализации репозиториев, мапперы, DTO сети | содержать экраны и тексты интерфейса |

### Экран по шаблону MVVM

- `XxxUiState` — data class со всем, что нужно нарисовать: данные, `isLoading`, текст ошибки.
- `XxxViewModel` хранит `StateFlow<XxxUiState>` и функции-действия (`onSaveClick()`, `onRatingSelected(rating)`).
- `XxxScreen` — `@Composable`, получает ViewModel через `koinViewModel()`, рисует состояние и вызывает действия. Внутри `XxxScreen` нет логики, кроме отображения.
- Отдельный `XxxContent(state, onAction)` без ViewModel — для `@Preview` и проще тестировать.

У каждого экрана предусмотрены пустое состояние, загрузка и ошибка (особенно у каталога: без интернета приложение не падает, а показывает сообщение — требование ТЗ).

## 4. Где что лежит: частые вопросы

| Нужно сделать | Куда |
|---|---|
| Новый экран | `features/<функция>/ui/` + маршрут в `core/navigation/Routes.kt` |
| Новая таблица или поле в базе | `core/database/` (Entity, DAO, версия базы в `TumanDatabase`) — согласовать с Бондаренко Е. |
| Кнопка, карточка, поле ввода, которые нужны на нескольких экранах | `core/uikit/` |
| Запрос к серверу каталога | `core/network/` (клиент) + `features/catalog/data/` (DTO и репозиторий) |
| Расчёт даты повторения | только `core-srs` |
| Серия дней, XP, достижения | `features/progress/domain/` (расчёт) и `data/` (хранение) |
| Ежедневное уведомление | `core/reminders/` |
| Текст интерфейса | `app/src/main/res/values/strings.xml`, не строкой в коде |
| Цвет, размер шрифта | `core/uikit/theme/`, не числом в экране |

## 5. Правила зависимостей

1. `features/a` не импортирует ничего из `features/b`. Если двум функциям нужно общее — перенесите его в `core`.
2. `core` не знает о `features`. Исключение одно: `core/navigation` перечисляет экраны.
3. `domain` не импортирует `android.*`, `androidx.room.*`, `retrofit2.*`, `androidx.compose.*`.
4. Room Entity и DTO сети не выходят за пределы `data` и `core/database`: наружу отдаются модели `domain`.
5. `core-srs` не зависит ни от одного модуля проекта.

## 6. Библиотеки

| Что | Библиотека | Как в CalorAI |
|---|---|---|
| UI | Jetpack Compose, Material 3 | да |
| Навигация | Navigation Compose | да |
| Внедрение зависимостей | Koin (`koin-androidx-compose`) | да |
| База данных | Room + KSP | да |
| Сеть | Retrofit + Kotlinx Serialization | да |
| Фоновые задачи | WorkManager | — (у нас напоминания) |
| Тесты | JUnit 4, `kotlinx-coroutines-test` | — |

Все версии — только в `gradle/libs.versions.toml`. Проект собирается на AGP 8.13, Kotlin 2.0.21 и compileSdk 36. Свежие версии AndroidX могут требовать AGP 9 и сломать сборку, а KSP должен совпадать с версией Kotlin (`2.0.21-1.0.x`). Добавили библиотеку — сразу проверьте `./gradlew assembleDebug`.

## 7. Именование

| Что | Как | Пример |
|---|---|---|
| Экран | `<Что>Screen` | `ReviewScreen` |
| ViewModel / состояние | `<Что>ViewModel`, `<Что>UiState` | `ReviewViewModel`, `ReviewUiState` |
| Модель domain | существительное | `Deck`, `Card`, `Achievement` |
| Репозиторий | интерфейс `<Что>Repository`, реализация `<Что>RepositoryImpl` | `DecksRepository` |
| Таблица Room / DAO | `<Что>Entity`, `<Что>Dao` | `CardEntity`, `CardDao` |
| Ответ сервера | `<Что>Dto` | `SetSummaryDto` |
| Модуль Koin | `<функция>Module` | `decksModule` |
| Пакеты | одним словом или camelCase | `bashkirKeyboard` |

Код и имена — на английском, комментарии и тексты коммитов — на русском.

## 8. Тесты

| Что тестируем | Где | Чем запускать |
|---|---|---|
| Алгоритм повторения | `core-srs/src/test/` | `./gradlew :core-srs:test` |
| ViewModel, расчёт серии и XP | `app/src/test/` (тот же пакет, что и код) | `./gradlew :app:testDebugUnitTest` |
| Room (сохранение и чтение) | `app/src/androidTest/` | Android Studio → Run на эмуляторе |

Всё, что не требует эмулятора, проверяется командой `./gradlew test`. На GitHub автоматические проверки отключены, поэтому эту команду каждый запускает у себя перед PR (`/finish-task` делает это сам).
