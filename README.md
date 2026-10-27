<div align="center">

# 🌫️ Туман — Языки России

**Android-приложение с карточками и адаптивным интервальным повторением для тех, кто сам изучает языки коренных народов России. В MVP — башкирский язык.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20%28API%2026%29-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-offline--first-0F9D58)](https://developer.android.com/training/data-storage/room)
[![FSRS](https://img.shields.io/badge/повторение-FSRS-F57C00)](docs/product.md)
[![ITMO](https://img.shields.io/badge/ИТМО-курсовой%20проект%202026-1946BA)](docs/tz.md)

[📋 Доска задач](https://github.com/orgs/tuman-langs/projects/1) · [📄 ТЗ](docs/tz.md) · [🏗 Архитектура](docs/architecture.md) · [👥 Команда](docs/team.md) · [🖥 Сервер каталога](https://github.com/tuman-langs/tuman-langs-backend)

</div>

---

## 📱 О приложении

**Туман** помогает регулярно заниматься языком без преподавателя. Пользователь скачивает готовые наборы карточек по башкирскому или создаёт свои, а приложение само решает, какие слова пора повторить сегодня. Слово, которое вы «не помните», вернётся завтра, а «лёгкое» — через неделю. Дневная цель, серия дней и достижения помогают не бросать.

Всё, кроме загрузки наборов, работает без интернета.

### ✨ Возможности MVP

| | Функция | Что умеет |
|---|---|---|
| 🗂 | **Наборы и карточки** | создать, изменить, удалить; лицевая и обратная сторона, пример |
| ⌨️ | **Башкирские буквы** | панель ә ө ү ғ ҡ ң ҙ ҫ һ над клавиатурой |
| ☁️ | **Каталог** | 3 готовых набора, 100+ карточек, в том числе по аффиксам; загрузка на устройство |
| 🔁 | **Повторение** | список «на сегодня» по упрощённому FSRS, оценка «не помню / трудно / хорошо / легко» |
| 🔥 | **Мотивация** | дневная цель, серия дней, очки опыта (XP), 5+ достижений, экран прогресса |
| 🔔 | **Напоминания** | ежедневное уведомление в выбранное время, можно отключить |

Полный список требований — [техническое задание](docs/tz.md).

---

## 🗓 Дорожная карта

| Контрольная точка | Что показываем | Этапы ТЗ |
|---|---|---|
| **26.10** | макеты и UI-kit в Figma, каркас приложения, база данных, заготовка алгоритма, первый сервер и первый набор | 1, 2 |
| **02.11** | сервер каталога развёрнут, API описан | 6 |
| **09.11** | наборы и карточки работают в APK, алгоритм с юнит-тестами, 3 набора (100+ карточек) | 3, 4, 5 |
| **16.11** | каталог и загрузка наборов, работа без интернета | 7 |
| **23.11** | повторение, мотивация, экран прогресса, напоминания | 8 |
| **30.11** | приёмочное тестирование, исправления, 🎓 защита | 9, 10 |

Текущее состояние — на [доске «Туман — MVP»](https://github.com/orgs/tuman-langs/projects/1).

---

## 👥 Команда

| Участник | Роль | Зона в коде |
|---|---|---|
| Туяков Мирхат | руководитель | ревью, приёмка, документация |
| Карпенко Анастасия | Android: экраны приложения | `core/navigation`, `core/network`, `core/uikit`, `features/home`, `decks`, `catalog` |
| Дворак Матвей | Android: экраны, алгоритм повторений | `core-srs`, `features/review`, `progress/ui`, `settings/ui` |
| Бондаренко Егор | Android: данные и напоминания | `core/database`, `core/reminders`, расчёты в `features/progress` |
| Василенко Арина | backend | [tuman-langs-backend](https://github.com/tuman-langs/tuman-langs-backend) `server/` |
| Потапенко Анна | UI/UX-дизайнер | макеты и UI-kit в Figma |
| Богданов Егор | контент-лид, лингвист | наборы в [tuman-langs-backend](https://github.com/tuman-langs/tuman-langs-backend) `content/` |
| Михеев Владислав | тестировщик | `docs/qa/`, задачи «Ошибка» |

Кто от кого зависит и почему роли распределены так — [docs/team.md](docs/team.md).

---

## 🛠 Технологический стек

| Слой | Технологии |
|---|---|
| Язык и платформа | **Kotlin** 2.0.21, Android SDK 26–36 |
| UI | **Jetpack Compose**, Material 3, Navigation Compose |
| Архитектура | Clean Architecture (`data` / `domain` / `ui`), **MVVM** со `StateFlow` |
| Внедрение зависимостей | **Koin** |
| Локальное хранилище | **Room** (SQLite) + KSP |
| Сеть | **Retrofit** + Kotlinx Serialization — только для каталога |
| Фоновые задачи | **WorkManager** — ежедневные напоминания |
| Алгоритм | модуль `core-srs`: упрощённый **FSRS** на чистом Kotlin |
| Сборка | Gradle 8.13, AGP 8.13, версии в `gradle/libs.versions.toml` |

---

## 🏗 Архитектура

```
Tuman/
├── app/                         приложение
│   └── src/main/java/ru/itmo/tuman/
│       ├── core/                общее: database · network · navigation · reminders · uikit · utils
│       ├── features/            по функции: home · decks · catalog · review · progress · settings
│       │   └── <функция>/       data · domain · ui · di
│       ├── di/                  сборка модулей Koin
│       ├── TumanApp.kt          Application
│       └── MainActivity.kt      единственная Activity
├── core-srs/                    алгоритм повторения, без Android, с юнит-тестами
└── docs/                        ТЗ, архитектура, команда, правила
```

```
Screen ──действие──▶ ViewModel ──▶ Repository (domain) ──▶ RepositoryImpl (data) ──▶ Room / сервер
  ▲                      │
  └──── UiState ◀────────┘
```

Слои, правила зависимостей, именование и таблица «куда положить файл» — [docs/architecture.md](docs/architecture.md).

---

## 🚀 Как запустить

**Нужно:** Android Studio (Narwhal или новее), JDK 21 (в новых версиях Android Studio встроена Java 25, она не подходит; как выбрать JDK 21 в Studio и в `JAVA_HOME` — [docs/onboarding.md](docs/onboarding.md#java-для-gradle-java_home)), эмулятор или телефон с Android 8.0+.

1. **Клонировать.** Android Studio → **File → New → Project from Version Control** → `https://github.com/tuman-langs/tuman-langs-mobile.git`. Или в терминале:
   ```bash
   git clone https://github.com/tuman-langs/tuman-langs-mobile.git
   ```
2. **Дождаться Gradle Sync.** Если Android Studio предлагает обновить библиотеки — не соглашайтесь (см. [CLAUDE.md](CLAUDE.md), «Проверка перед PR»).
3. **Собрать и проверить:**
   ```bash
   ./gradlew test assembleDebug        # Windows: .\gradlew.bat test assembleDebug
   ```
4. **Запустить:** кнопка **Run ▶** в Android Studio. Проверяйте и на эмуляторе с API 26 — это минимальная версия по ТЗ.

Первый день в проекте (доступы, установка Git, `gh`, Claude Code) — [docs/onboarding.md](docs/onboarding.md).

---

## 🔄 Как мы работаем

```
Issue на доске («На неделю»)
   │  /start-task N
   ▼
ветка feature/N-… («В работе») ── коммиты «#N: что сделано»
   │  /finish-task
   ▼
PR в main («На ревью») ◀── /fix-review ◀── замечания ревьюера
   │  одобрено → Squash and merge
   ▼
issue закрыт, карточка «Готово»
```

- **Одна задача — одна ветка — один PR.** Ветка `<тип>/<номер>-<кратко>`: `feature/`, `fix/`, `test/`, `docs/`, `content/`, `design/`.
- В `main` — только через pull request с одобрением ревьюера, прямой push закрыт.
- В каждом PR — раздел **«Использование ИИ»** (требование ИТМО).
- **Проверки запускаются локально:** GitHub Actions в репозитории выключены, поэтому `./gradlew test` перед PR обязателен.

Колонки доски, метки, ритм недели и Definition of Done — [docs/kanban-workflow.md](docs/kanban-workflow.md).

---

## 🤖 Работа с Claude Code

Контекст проекта лежит в репозитории: Claude Code читает [CLAUDE.md](CLAUDE.md) и `docs/architecture.md` и сразу знает правила. Команды проекта:

| Команда | Когда | Что делает |
|---|---|---|
| `/my-tasks` | в начале дня | мои задачи, PR и запросы на ревью, следующий шаг |
| `/start-task 12` | беру задачу | читает issue, создаёт ветку, составляет план по архитектуре |
| `/finish-task` | задача готова | свежий main, проверки, секреты, коммит, PR по шаблону |
| `/review-pr 15` | проверяю чужой PR | сверяет с задачей и архитектурой, готовит замечания |
| `/fix-review` | пришли замечания | разбирает их, правит в той же ветке, отвечает ревьюеру |
| `/explain-my-work` | перед защитой | объясняет сделанное и задаёт контрольные вопросы |

Как проверять ответы ИИ и что писать в декларации — [docs/claude-for-team.md](docs/claude-for-team.md).

---

## 📚 Документация

| Документ | О чём |
|---|---|
| [CLAUDE.md](CLAUDE.md) | контекст проекта и правила для Claude Code |
| [docs/tz.md](docs/tz.md) | техническое задание |
| [docs/architecture.md](docs/architecture.md) | модули, пакеты, слои, куда класть файлы |
| [docs/product.md](docs/product.md) | продукт, FSRS, формат наборов |
| [docs/team.md](docs/team.md) | роли, зоны ответственности, зависимости |
| [docs/kanban-workflow.md](docs/kanban-workflow.md) | доска, метки, ритм недели, Definition of Done |
| [docs/onboarding.md](docs/onboarding.md) | первый день: доступы, установка, путь задачи |
| [docs/claude-for-team.md](docs/claude-for-team.md) | работа через Claude, декларация ИИ |

---

<div align="center">

**Курсовой проект ИТМО · осень 2026**

Сделано с заботой о языках России 🌲

</div>
