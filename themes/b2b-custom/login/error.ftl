<#-- error.ftl -->
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Error</title>
    <meta charset="UTF-8"/>
</head>
<body>
    <h2>Error</h2>
    <div>
        <#if message?has_content>
            <p>${message.summary!}</p>
        <#else>
            <p>Unexpected error occurred.</p>
        </#if>
    </div>
</body>
</html>
