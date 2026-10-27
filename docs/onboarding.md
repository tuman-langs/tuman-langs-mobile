# Первый день в проекте

Порядок для каждого участника. Займёт около часа; если что-то не выходит — пишите руководителю в чат команды.

## 1. Доступ к GitHub

1. Зарегистрируйтесь на https://github.com (если аккаунта нет) и пришлите руководителю свой **логин GitHub**.
2. Придёт письмо с приглашением в организацию проекта. Примите его в течение 7 дней — потом приглашение сгорает. Если письма нет, откройте https://github.com/orgs/tuman-langs/invitation, войдя в свой аккаунт.
3. Проверьте, что видите репозитории https://github.com/tuman-langs/tuman-langs-mobile, https://github.com/tuman-langs/tuman-langs-backend и доску «Туман — MVP» (вкладка Projects организации).

## 2. Установка

| Что | Кому | Как |
|---|---|---|
| Git | всем | https://git-scm.com/downloads (на Windows — Git for Windows) |
| Android Studio | Android-разработчикам, тестировщику | https://developer.android.com/studio |
| GitHub CLI (`gh`) | тем, кто работает с Claude Code | https://cli.github.com, затем `gh auth login` |
| Claude Code | по желанию, нужен платный тариф Claude | Windows PowerShell: `irm https://claude.ai/install.ps1 \| iex`; macOS/Linux: `curl -fsSL https://claude.ai/install.sh \| bash` |
| Плагин Claude Code [Beta] | работающим в Android Studio | Settings → Plugins → Marketplace → «Claude Code» |

Представьтесь Git один раз:
```bash
git config --global user.name "Имя Фамилия"
git config --global user.email "почта-как-на-github@example.com"
```

### Java для Gradle (`JAVA_HOME`)

Кнопки в Android Studio сами используют встроенную в неё Java 21 (JBR). А `gradlew` в терминале берёт Java из переменной `JAVA_HOME`, а если её нет — первую `java` из системного PATH. Если там установлена Java 24 или новее, Gradle 8.13 падает с непонятной ошибкой:

```
Could not create task ':app:testDebugUnitTest'.
   ...
      > Type T not present
```

Поэтому один раз укажите `JAVA_HOME` на Java из Android Studio. Путь зависит от того, куда вы её установили; по умолчанию:

| Система | Путь к Java из Android Studio |
|---|---|
| Windows | `C:\Program Files\Android\Android Studio\jbr` |
| macOS | `/Applications/Android Studio.app/Contents/jbr/Contents/Home` |

**Windows (PowerShell)** — насовсем для вашего пользователя, затем закройте и снова откройте терминал (и Android Studio, если терминал в ней):
```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Android\Android Studio\jbr", "User")
```
То же через интерфейс: «Параметры» → поиск «переменные среды» → «Изменить переменные среды для вашей учётной записи» → «Создать».

**macOS** — добавьте строку в `~/.zshrc` и откройте новый терминал:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

**Проверка:** `./gradlew --version` (на Windows `.\gradlew.bat --version`) — в строке `Launcher JVM` должно быть **21.x (JetBrains)**. Если видите 24 — переменная не подхватилась, откройте терминал заново.

Не поднимайте версию Gradle, чтобы «починить» ошибку: она подобрана под Android Gradle Plugin 8.13 (см. `CLAUDE.md`).

## 3. Получить проект

Android Studio → **File → New → Project from Version Control** → `https://github.com/tuman-langs/tuman-langs-mobile.git` → Clone. Backend-разработчику — ещё и `tuman-langs-backend`. При первом входе Android Studio попросит авторизоваться в GitHub через браузер.

Без Android Studio (дизайнер, контент-лид): можно работать прямо на сайте GitHub — открыть файл, нажать карандаш, а при сохранении выбрать **«Create a new branch for this commit and start a pull request»**.

## 4. Claude Code (если установлен)

В Android Studio откройте терминал (View → Tool Windows → Terminal) в корне проекта и запустите `claude`. Он сам прочитает `CLAUDE.md` и узнает о проекте. Проверьте: «Кратко: что за проект и что входит в MVP?»

Команды проекта:
- `/my-tasks` — мои задачи, PR и запросы на ревью, что делать дальше;
- `/start-task 12` — прочитать задачу #12, создать ветку с правильным именем, составить план по архитектуре;
- `/finish-task` — подтянуть main, запустить проверки, закоммитить, отправить, открыть PR по шаблону;
- `/review-pr 15` — помочь сделать ревью чужого PR #15;
- `/fix-review` — разобрать замечания к своему PR и исправить их;
- `/explain-my-work` — объяснить сделанное и задать контрольные вопросы (подготовка к защите).

Перед первой задачей с кодом прочитайте `docs/architecture.md`: там написано, в какой папке лежит ваша часть приложения.

Подробно о правилах работы с ИИ — `docs/claude-for-team.md`.

## 5. Путь задачи

```
Бэклог → На неделю → В работе → На ревью → Готово
```

1. **Задача.** Руководитель создаёт issue и ставит вас ответственным. На понедельничном созвоне задача попадает в «На неделю».
2. **Начали.** Перенесите карточку в «В работе». Создайте ветку: в issue справа **Development → Create a branch**, имя `<тип>/<номер>-<кратко>`, например `feature/12-card-editor` (или `/start-task 12`). В Android Studio: **Git → Fetch**, затем **Git → Branches → origin/feature/12-card-editor → Checkout**.
3. **Работаете.** Коммитьте понемногу (**Git → Commit**, Ctrl+K), сообщение: `#12: что сделано`. Отправляйте: **Git → Push** (Ctrl+Shift+K).
4. **Pull request.** На GitHub появится кнопка «Compare & pull request». База — `main`. Заполните шаблон: что сделано, `Closes #12`, как проверить, **использование ИИ**. Ревьюер — руководитель. Карточку — в «На ревью».
5. **Ревью.** Ответьте на комментарии, исправьте в той же ветке и снова Push — PR обновится сам.
6. **Слияние.** После одобрения нажимается **Squash and merge**. Issue закроется, карточка уйдёт в «Готово», ветка удалится. У себя: **Git → Branches → main → Checkout**, затем **Git → Pull**.

Типы веток: `feature/` — новая функция, `fix/` — исправление ошибки, `test/` — тесты, `docs/` — документация, `content/` — наборы карточек, `design/` — экспорт макетов.

На GitHub автоматических проверок нет (Actions отключены), поэтому перед PR обязательно запустите у себя `./gradlew test` и `./gradlew assembleDebug` — `/finish-task` делает это сам.

Перед PR подтяните свежий `main` (**Git → Pull**, ветка `origin/main`, или `git pull origin main`), чтобы конфликты решить у себя, а не в PR.

## 6. Правила

- Одна задача — одна ветка — один PR. В `main` напрямую не пушим.
- Пароли, токены, `.env`, `local.properties` — никогда не коммитим.
- Карточка на доске всегда показывает правду: преподаватели видят её копию в EduFlow.
- В воскресенье вечером обновите свои карточки и оставьте короткий итог в issue.
