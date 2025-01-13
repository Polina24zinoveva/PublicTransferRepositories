<!DOCTYPE html>

<head>
    <meta charset="UTF-8">
    <title>Transfer of Repositories</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">

    <style>

        .container-custom{
            margin-top: 4rem;
            margin-left: 4rem;
            margin-right: 4rem;
        }


        .nav-link.active {
            color: #333;
            font-size: 18px;
            font-weight: 600;
        }

        .nav-link {
            color: #333;
            font-size: 18px;

            display: flex;
            align-items: center;
        }

        .nav-link svg {
            margin-right: 6px;
        }

        .nav-link:hover {
            color: #fc6d26;
            text-decoration: none;
        }

        .nav-link:focus {
            color: #fc6d26;
        }

        .btn-custom-save {
            border-color: #B22D47;
            color: #B22D47;
        }

        .btn-custom-save:hover {
            background-color: #B22D47;
            color: #ffffff;
            border-color: #B22D47;
        }

        .btn-custom-update  {
            border-color: #fc6d26;
            color: #fc6d26;
        }

        .btn-custom-update:hover {
            background-color: #fc6d26;
            color: #ffffff;
            border-color: #fc6d26;
        }

        .btn-custom-all {
            border-color: #fca326;
            color: #fca326;
            margin-right: 8px;
        }

        .btn-custom-all:hover{
            background-color: #fca326;
            color: #ffffff;
            border-color: #fca326;
        }

        .btn-disabled {
            border-color: transparent;
            color: transparent;
            background-color: transparent;
            pointer-events: none;
            cursor: not-allowed;
        }

        .saveP {
            color: #ABABAB;
        }

        .table tbody tr {
            height: 54px;
        }

    </style>

    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script>
        function showMessage(message) {
            Swal.fire({
                text: message,
                icon: "success",
                confirmButtonColor: '#28a745'
            });
        }
        function showError(error) {
            Swal.fire({
                text: error,
                icon: "error",
                confirmButtonColor: "#dd3333",
            });
        }
    </script>

</head>

<body>
    <#if message??>
        <script>
            showMessage("${message}");
        </script>
    </#if>
    <#if error??>
        <script>
            showError("${error}");
        </script>
    </#if>
    <div class="container-custom">
        <h4 class="text-center">Сервис по переносу репозиториев из GitLab в GitHub через локальный диск</h4>
        <ul class="nav nav-tabs" style="margin-top: 4rem;">
            <li class="nav-item">
                <a class="nav-link" href="/gitlab">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-gitlab" viewBox="0 0 16 16">
                      <path d="m15.734 6.1-.022-.058L13.534.358a.57.57 0 0 0-.563-.356.6.6 0 0 0-.328.122.6.6 0 0 0-.193.294l-1.47 4.499H5.025l-1.47-4.5A.572.572 0 0 0 2.47.358L.289 6.04l-.022.057A4.044 4.044 0 0 0 1.61 10.77l.007.006.02.014 3.318 2.485 1.64 1.242 1 .755a.67.67 0 0 0 .814 0l1-.755 1.64-1.242 3.338-2.5.009-.007a4.05 4.05 0 0 0 1.34-4.668Z"/>
                    </svg>
                    GitLab
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="/disk">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-floppy-fill" viewBox="0 0 16 16">
                      <path d="M0 1.5A1.5 1.5 0 0 1 1.5 0H3v5.5A1.5 1.5 0 0 0 4.5 7h7A1.5 1.5 0 0 0 13 5.5V0h.086a1.5 1.5 0 0 1 1.06.44l1.415 1.414A1.5 1.5 0 0 1 16 2.914V14.5a1.5 1.5 0 0 1-1.5 1.5H14v-5.5A1.5 1.5 0 0 0 12.5 9h-9A1.5 1.5 0 0 0 2 10.5V16h-.5A1.5 1.5 0 0 1 0 14.5z"/>
                      <path d="M3 16h10v-5.5a.5.5 0 0 0-.5-.5h-9a.5.5 0 0 0-.5.5zm9-16H4v5.5a.5.5 0 0 0 .5.5h7a.5.5 0 0 0 .5-.5zM9 1h2v4H9z"/>
                    </svg>
                    Диск
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="/github">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-github" viewBox="0 0 16 16">
                      <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27s1.36.09 2 .27c1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8"/>
                    </svg>
                    GitHub
                </a>
            </li>
        </ul>

        <div class="d-flex align-items-center" style="margin-top: 3rem;">
            <h4 style="margin-left: 7px;" class="mb-0 align-items-center">Репозитории</h4>
            <#if allRepositoriesFromGitlab??>
                <form action="/downloadFromGitlabToDiskAllRepositories" method="post" id="downloadForm" class="ms-auto" style="margin-top: 1rem;">
                    <button id="downloadButton" style="width: 276px" class="btn btn-custom-all float-end" type="submit" onclick="showLoadingIndicatorAllRepository()">
                        <span class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                        Скачать все репозитории на диск
                    </button>
                </form>
                <script>
                    function showLoadingIndicatorAllRepository() {
                        var button = document.getElementById('downloadButton');
                        var form = document.getElementById('downloadForm');
                        button.disabled = true; // Отключаем кнопку, чтобы избежать повторных отправок
                        var spinner = button.querySelector('.spinner-border');
                        spinner.classList.remove('d-none'); // Показываем индикатор загрузки
                        button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Скачивание';

                        var downloadButtons = document.querySelectorAll('.download-button');
                        downloadButtons.forEach(function(btn) {
                            btn.click();
                        });

                        form.submit(); // Отправляем форму
                    }
                </script>
            <#else>
                <#if allRepositoriesFromDisk??>
                    <form action="/downloadFromDiskToGithubAllRepositories" method="post" id="downloadForm" class="ms-auto" style="margin-top: 1rem;">
                        <button id="downloadButton" style="width: 19rem" class="btn btn-custom-all float-end" type="submit" onclick="showLoadingIndicatorAllRepository()">
                            <span class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                            Загрузить все репозитории в github
                        </button>
                    </form>
                    <script>
                        function showLoadingIndicatorAllRepository() {
                            var button = document.getElementById('downloadButton');
                            var form = document.getElementById('downloadForm');
                            button.disabled = true; // Отключаем кнопку, чтобы избежать повторных отправок
                            var spinner = button.querySelector('.spinner-border');
                            spinner.classList.remove('d-none'); // Показываем индикатор загрузки
                            button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Загрузка';

                            var downloadButtons = document.querySelectorAll('.download-button');
                            downloadButtons.forEach(function(btn) {
                                btn.click();
                            });

                            form.submit(); // Отправляем форму
                        }
                    </script>
                <#else>
                    <form id="downloadForm" class="ms-auto" style="margin-top: 1rem;">
                        <button id="downloadButton" style="width: 8rem" class="btn btn-disabled float-end">
                            <span class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                            Загрузить
                        </button>
                    </form>
                </#if>
            </#if>
        </div>
        <script>
            // Получаем текущий путь
            var currentPath = window.location.pathname;

            // Получаем все ссылки с классом .nav-link
            var navLinks = document.querySelectorAll('.nav-link');

            // Проходим по каждой ссылке
            navLinks.forEach(function(link) {
                // Если текущий путь совпадает с атрибутом href этой ссылки
                if (link.getAttribute('href') === currentPath) {
                    // Добавляем класс активности
                    link.classList.add('active');
                } else {
                    // Удаляем класс активности, если путь не совпадает
                    link.classList.remove('active');
                }
            });
        </script>
        <#if allRepositoriesFromGitlab??>
            <table class="table" style="margin-top: 2rem;">
                <thead>
                <tr></tr>
                <tr></tr>
                </thead>
                <tbody>
                    <#list allRepositoriesFromGitlab as repository>
                        <tr class="align-items-center">
                            <td  class="align-middle">${repository.name}</td>

                            <td>
                                <form action="/downloadFromGitlabToDisk/${repository.name}" method="post" id="downloadForm-${repository.name}" class="d-flex align-items-center float-end">
                                    <#if repository.saveOnDisk == true>
                                        <p class="saveP mb-0 me-3">
                                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-check2" viewBox="0 0 16 16">
                                              <path d="M13.854 3.646a.5.5 0 0 1 0 .708l-7 7a.5.5 0 0 1-.708 0l-3.5-3.5a.5.5 0 1 1 .708-.708L6.5 10.293l6.646-6.647a.5.5 0 0 1 .708 0"/>
                                            </svg>
                                            есть на диске
                                        </p>
                                        <button id="downloadButton-${repository.name}" style="width: 9rem" class="btn btn-custom-update download-button float-end" type="submit" onclick="showLoadingIndicator('${repository.name}', 'update')">
                                            <span class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                                            Обновить
                                        </button>
                                    <#else>
                                        <p class="saveP mb-0 me-3"></p>
                                        <button id="downloadButton-${repository.name}" style="width: 9rem" class="btn btn-custom-save download-button float-end" type="submit" onclick="showLoadingIndicator('${repository.name}', 'save')">
                                            <span class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                                            Скачать
                                        </button>
                                    </#if>
                                </form>

                                <script>
                                    function showLoadingIndicator(repositoryName, text) {
                                        var button = document.getElementById('downloadButton-' + repositoryName);
                                        var form = document.getElementById('downloadForm-' + repositoryName);
                                        button.disabled = true; // Отключаем кнопку, чтобы избежать повторных отправок
                                        var spinner = button.querySelector('.spinner-border');
                                        spinner.classList.remove('d-none'); // Показываем индикатор загрузки
                                        if (text == "save"){
                                            button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Скачивание';
                                        }
                                        else button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Обновление';
                                        form.submit();
                                    }
                                </script>
                            </td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        <#else>
            <#if allRepositoriesFromDisk??>
                <table class="table" style="margin-top: 2rem;">
                    <thead>
                    <tr></tr>
                    <tr></tr>
                    </thead>
                    <tbody>
                        <#list allRepositoriesFromDisk as repository>
                            <tr class="align-items-center">
                                <td class="align-middle">${repository.name}</td>

                                <td>
                                    <form action="/downloadFromDiskToGithub/${repository.name}" method="post" id="downloadForm-${repository.name}" class="d-flex align-items-center float-end">
                                        <#if repository.saveOnGithub == true>
                                            <p class="saveP mb-0 me-3">
                                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-check2" viewBox="0 0 16 16">
                                                  <path d="M13.854 3.646a.5.5 0 0 1 0 .708l-7 7a.5.5 0 0 1-.708 0l-3.5-3.5a.5.5 0 1 1 .708-.708L6.5 10.293l6.646-6.647a.5.5 0 0 1 .708 0"/>
                                                </svg>
                                                есть в github
                                            </p>
                                            <button id="downloadButton-${repository.name}" style="width: 9rem" class="btn btn-custom-update download-button float-end" type="submit" onclick="showLoadingIndicator('${repository.name}', 'update')">
                                                <span class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                                                Обновить
                                            </button>
                                        <#else>
                                            <p class="saveP mb-0 me-3"></p>
                                            <button id="downloadButton-${repository.name}" style="width: 9rem" class="btn btn-custom-save download-button float-end" type="submit" onclick="showLoadingIndicator('${repository.name}', 'save')">
                                                <span class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                                                Загрузить
                                            </button>
                                        </#if>
                                    </form>

                                    <script>
                                        function showLoadingIndicator(repositoryName, text) {
                                            var button = document.getElementById('downloadButton-' + repositoryName);
                                            var form = document.getElementById('downloadForm-' + repositoryName);
                                            button.disabled = true; // Отключаем кнопку, чтобы избежать повторных отправок
                                            var spinner = button.querySelector('.spinner-border');
                                            spinner.classList.remove('d-none'); // Показываем индикатор загрузки
                                            if (text == "save"){
                                                button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Загрузка';
                                            }
                                            else button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Обновление';                                            form.submit(); // Отправляем форму
                                            }
                                            form.submit();
                                    </script>
                                </td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            <#else>
                <#if allRepositoriesFromGithub??>
                    <table class="table" style="margin-top: 2rem;">
                        <thead>
                        <tr></tr>
                        </thead>
                        <tbody>
                            <#list allRepositoriesFromGithub as repository>
                                <tr class="align-items-center">
                                    <td class="align-middle">${repository.name}</td>
                                </tr>
                            </#list>
                        </tbody>
                    </table>
                </#if>
            </#if>
        </#if>
    </div>
</body>

</html>