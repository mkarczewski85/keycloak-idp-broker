<#-- email-form.ftl -->
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Sign in with Email</title>
    <meta charset="UTF-8"/>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
            padding: 0;
        }
        .form-box {
            max-width: 380px;
            margin: 60px auto;
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 2px 18px #0001;
            padding: 36px 32px 28px 32px;
            display: flex;
            flex-direction: column;
            align-items: stretch;
        }
        .form-box h2 {
            text-align: center;
            margin-bottom: 12px;
            font-size: 2rem;
            color: #1648aa;
            letter-spacing: 1px;
        }
        .form-box .instructions {
            text-align: center;
            color: #444;
            font-size: 1.06rem;
            margin-bottom: 20px;
        }
        .form-box input[type="email"] {
            width: 100%;
            padding: 14px 12px;
            margin-bottom: 22px;
            border: 1.5px solid #bdd6fb;
            border-radius: 5px;
            font-size: 1rem;
            background: #f8fbff;
            box-sizing: border-box;
            outline: none;
            transition: border 0.2s;
        }
        .form-box input[type="email"]:focus {
            border: 1.5px solid #4183f7;
            background: #fff;
        }
        .form-box button {
            width: 100%;
            padding: 13px 0;
            background: #1648aa;
            color: #fff;
            border: none;
            border-radius: 5px;
            font-size: 1.09rem;
            font-weight: 500;
            cursor: pointer;
            transition: background 0.18s;
            margin-bottom: 10px;
            box-shadow: 0 1px 3px #0001;
        }
        .form-box button:hover, .form-box button:focus {
            background: #2965d6;
        }
        .form-box .kc-feedback-text {
            color: #e14c3a;
            margin-bottom: 16px;
            text-align: center;
            font-size: 1rem;
            font-weight: 500;
        }
    </style>
</head>
<body>
<div class="form-box">
    <h2>Sign in</h2>
    <div class="instructions">
        Provide your email to proceed.
    </div>
    <#if message?has_content>
        <div class="kc-feedback-text">${message.summary!}</div>
    </#if>
    <form id="kc-form-login" action="${url.loginAction}" method="post" autocomplete="on">
        <input type="email" id="username" name="username" value="${username!}" placeholder="Email address" autofocus required autocomplete="username email"/>
        <button type="submit" id="kc-login">Sign in</button>
        <input type="hidden" name="client_id" value="${client.clientId!}" />
    </form>
</div>
</body>
</html>
