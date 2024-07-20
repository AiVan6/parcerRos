package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static Browser browser;
    private static BrowserContext browserContext;
    private static final String cookies = "cookies.json";
    private static final Path path = Paths.get(cookies);
//    public static BrowserContext login(Browser browser){
//
//        BrowserContext browserContext = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
//        Page page = browserContext.newPage();
//        page.navigate("https://lk.rosreestr.ru/login?redirect=%2F");
//        page.waitForLoadState(LoadState.NETWORKIDLE);
//        boolean flag = true;
//        // Заполняем поля логина и пароля
//        while (flag) {
//            System.out.print("Введите логин: ");
//            Scanner loginScanner = new Scanner(System.in);
//            String loginStr = loginScanner.nextLine();
//            ElementHandle login = page.querySelector("#login");
//            if(login != null){
//                login.fill("");
//                login.fill(loginStr);
//            }
//
//            System.out.print("Введите пароль: ");
//            String password = loginScanner.nextLine();
//            ElementHandle passwordEH = page.querySelector("#password");
//            if(passwordEH != null){
//                passwordEH.fill("");
//                passwordEH.fill(password);
//            }
//            if(!password.isEmpty() && !loginStr.isEmpty()){flag=false;}
//        }
//        page.click("button[class = 'plain-button plain-button_wide']");
//        System.out.println();
//        page.waitForLoadState(LoadState.NETWORKIDLE);
//
//        // Вводим код подтверждения вручную
//        System.out.print("Введите код подтверждения: ");
//        Scanner codeScanner = new Scanner(System.in);
//        page.fill("input[type='tel']", codeScanner.nextLine());
//
//        System.out.println();
//        return browserContext;
//    }

    public static void menu(){
        System.out.println("Выбери действие:");
        System.out.println("1. Добавить кадастровые номера");
        System.out.println("2. Скачать");
        System.out.println("0. Выход");
        System.out.println();
    }

    public static void checkSession(Page page){

        System.out.println("Сессия истекла, ещё раз авторизирую тебя");
        page.reload();
        page.waitForLoadState(LoadState.NETWORKIDLE);

        browserContext = browser.newContext(
                new Browser.NewContextOptions().setStorageStatePath(path).setIgnoreHTTPSErrors(true));

    }

    public static void main(String[] args) {


        try (Playwright playwright = Playwright.create()) {

            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));

            browserContext = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
            Page page = browserContext.newPage();
            page.waitForTimeout(30000);
            page.navigate("https://lk.rosreestr.ru/login?redirect=%2F");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("page.png")));
            DataParser dataParser = new DataParser(page);

//            String cookies = "cookies.json";

            File authFile = new File(cookies);

            authFile.delete();

            dataParser.authorization();


            browserContext.storageState(new BrowserContext.StorageStateOptions().setPath(path));

//            BrowserContext context = browser.newContext(
//                    new Browser.NewContextOptions().setStorageStatePath(path));

//            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("login.png")));

            page.waitForLoadState(LoadState.NETWORKIDLE);
            Page key = browser.newPage();
            key.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("login.png")));
            key.navigate("https://lk.rosreestr.ru/my-keys");
            key.waitForLoadState(LoadState.NETWORKIDLE);

//            Page page = browserContext.newPage();
            page.navigate("https://lk.rosreestr.ru/request-access-egrn/my-claims");
            page.waitForLoadState(LoadState.NETWORKIDLE);
//            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("page.png")));
            System.out.println("Привет, желаю тебе не хорошего дня, а замечательного!");
//            DataParser dataParser = new DataParser(page);

            while (true) {
                System.out.println();
                menu();
                System.out.println("Скачал: " + dataParser.getLoad());
                System.out.println("Отправил: " + dataParser.getSent());
                System.out.println("Не отправил: " + dataParser.getNotSent());
                System.out.println();
                System.out.print("Введите цифру: ");
                Scanner switchScanner = new Scanner(System.in);
                switch (switchScanner.nextInt()) {

                    case 0: {
                        return;
                    }

                    case 1: {
                        try {
                            System.out.print("Введите путь к файлу: ");
                            Scanner scanner = new Scanner(System.in);
                            if (dataParser.isFile(new File(scanner.nextLine()))) {
                                System.out.println("Файл не загружен, доступен файл только '.txt'");
                                break;
                            }
                            System.out.println("Начинаю добавлять");
                            dataParser.addNumbers();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    }

                    case 2: {
                        System.out.println("Приступаю к скачиванию");
                        dataParser.downloadData();
                        break;
                    }

                    default:
                        System.out.println("Неверная команда");
                        break;
                }
            }
        }
    }
}