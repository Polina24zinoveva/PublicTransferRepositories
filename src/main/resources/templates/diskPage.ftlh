<!DOCTYPE html>

<head>
  <meta charset="UTF-8">
  <title>Transfer of Repositories</title>
</head>

<body>
    <h4>Меню</h4>
    <p><a href="/">Репозитории на gitLab</a></p>
    <p><a href="/diskPage">Репозитории на диске</a></p>
    <p><a href="/gitHubPage">Репозитории на gitHub</a></p>

    <br>
    <h4>Репозитории на диске </h4>
    <form action="/downloadFromDiskToGithubAllRepositories" method="post">
        <button type="submit">Загрузить все репозитории на github</button>
    </form>
    <br><br>

    <#if allRepositories??>
        <table class="table">
            <thead>
            <tr></tr>
            <tr></tr>
        </thead>
            <tbody>
                <#list allRepositories as repository>
                    <tr>
                        <td>${repository}</td>

                        <td>
                            <form action="/downloadFromDiskToGithub/${repository}" method="post">
                                <button type="submit">Загрузить в github</button>
                            </form>
                        </td>

                    </tr>
                </#list>
            </tbody>
        </table>
    </#if>
</body>

</html>