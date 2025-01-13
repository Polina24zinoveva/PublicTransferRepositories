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
    <h4>Репозитории на github</h4>
    <#if allRepositories??>
        <table class="table">
            <thead>
            <tr></tr>
            <tr></tr>
            </thead>
            <tbody>

                <#list allRepositories as repository>
                    <tr>
                        <td>${repository.name}</td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </#if>
</body>

</html>