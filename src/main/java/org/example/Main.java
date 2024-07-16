package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import java.io.File;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {


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

    public static void main(String[] args) {


        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext browserContext;

            browserContext = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
            Page page = browserContext.newPage();
            page.waitForTimeout(30000);
            page.navigate("https://lk.rosreestr.ru/login?redirect=%2F");
            page.waitForLoadState(LoadState.NETWORKIDLE);
//            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("page.png")));

            boolean flag = true;
            // Заполняем поля логина и пароля
            while (flag) {
                System.out.print("Введите логин: ");
                Scanner loginScanner = new Scanner(System.in);
                String loginStr = loginScanner.nextLine();
                ElementHandle login = page.querySelector("#login");
                if(login != null){
                    login.fill("");
                    login.fill(loginStr);
                }

                System.out.print("Введите пароль: ");
                String password = loginScanner.nextLine();
                ElementHandle passwordEH = page.querySelector("#password");
                if(passwordEH != null){
                    passwordEH.fill("");
                    passwordEH.fill(password);
                }
                if(!password.isEmpty() && !loginStr.isEmpty()){flag=false;}
            }
            page.click("button[class = 'plain-button plain-button_wide']");
            System.out.println();
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // Вводим код подтверждения вручную
            System.out.print("Введите код подтверждения: ");
            Scanner codeScanner = new Scanner(System.in);
            page.fill("input[type='tel']", codeScanner.nextLine());
            page.waitForTimeout(30000);
//            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("login.png")));

//            page.waitForLoadState(LoadState.NETWORKIDLE);
            System.out.println();

            Page key = browserContext.newPage();
            key.navigate("https://lk.rosreestr.ru/my-keys");
//            key.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("login.png")));
            key.waitForLoadState(LoadState.NETWORKIDLE);

//            Page page = browserContext.newPage();
            page.navigate("https://lk.rosreestr.ru/request-access-egrn/my-claims");
            page.waitForLoadState(LoadState.NETWORKIDLE);
//            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("page.png")));
            System.out.println("Привет, желаю тебе не хорошего дня, а замечательного!");
            DataParser dataParser = new DataParser(page);

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