package org.example.parserrosreestrkad;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ElementState;
import com.microsoft.playwright.options.LoadState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class AppController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label labelAuthorization;
    @FXML
    private TextField filePathField;
    @FXML
    private Label loadFile;
    @FXML
    private TextField verifCode;
    private File file;

    private Browser browser;
    private BrowserContext browserContext;
    private Page page;


    private long error = 0;
    private long sentLong = 0;
    @FXML
    private Label sent;
    @FXML
    private Label notSent;
    @FXML
    private Label download;

    private String contextState = "contextState.json";
    boolean loginClickedBoll = true;
    boolean passwordClickedBoll = true;
    boolean codeClickedBoll = true;

    private  Map<String,String> map = new HashMap<>();



    @FXML
    protected void loginClicked() {
        if(loginClickedBoll) {
            loginField.setText("");
            loginClickedBoll = false;
        }
    }
    @FXML
    protected void passwordClicked() {
        if(passwordClickedBoll) {
            passwordField.setText("");
            passwordClickedBoll = false;
        }
    }

    @FXML
    protected void codeClicked() {
        if(codeClickedBoll) {
            verifCode.setText("");
            codeClickedBoll = false;
        }
    }

    @FXML
    protected void addFile() {
        System.out.println("addTemplateFile");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");

        File file = fileChooser.showOpenDialog(new Stage());
        if(file == null) {
            loadFile.setText("Файл не загружен, доступен файл только '.txt'");
            return;
        }
        Path path = Paths.get(file.getAbsolutePath());
        String fileType = "";
        try {
            String mimeType = Files.probeContentType(path);
            System.out.println(mimeType);
            fileType = mimeType == null ? "" : mimeType;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(file != null && fileType.equals("text/plain")) {
            filePathField.setText("");
            filePathField.setText(file.getAbsolutePath());
            this.file = file;
            loadFile.setText("Файл загружен");
        }else {
            loadFile.setText("Файл не загружен, доступен файл только '.txt'");
        }
    }

    @FXML
    private void handlerLoginAction() {
        String login = loginField.getText();
        String password = passwordField.getText();

        try {
            if (Paths.get(contextState).toFile().exists()) {

                String contextStateJson = new String(Files.readAllBytes(Paths.get(contextState)));

                browserContext = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true).setStorageState(contextStateJson));
                labelAuthorization.setText("Автоматом авторизирован");
            } else {
                loginToRosreestr(login, password);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void loginToRosreestr(String login, String password) {
        try {
//            if (Paths.get(contextState).toFile().exists()) {
//                String contextStateJson = new String(Files.readAllBytes(Paths.get(contextState)));
//
//                browserContext = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true).setStorageState(contextStateJson));
//                labelAuthorization.setText("Автоматом авторизирован");
//            } else {
                browserContext = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
                page.navigate("https://lk.rosreestr.ru/login?redirect=%2F");
                page.waitForLoadState(LoadState.NETWORKIDLE);
                page.fill("#login", login);
                page.fill("#password", password);
                page.click("button[class='plain-button plain-button_wide']");
                page.waitForLoadState(LoadState.NETWORKIDLE);

//                System.out.println(page.content());
//                browserContext.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get("contextState1.json")));

//            }
            labelAuthorization.setText("Введите код подтверждения");
        } catch (PlaywrightException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void searchKagNumber() {

        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String line;
        page.navigate("https://lk.rosreestr.ru/my-keys");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("key.png")));
        try {
            while ((line = reader.readLine()) != null) {
                try {
                    boolean flag = true;
                    page.navigate("https://lk.rosreestr.ru/request-access-egrn/property-search");
                    page.waitForLoadState(LoadState.NETWORKIDLE);

                    page.fill("#react-select-3-input",line);

                    page.waitForLoadState(LoadState.NETWORKIDLE);

//                        page.waitForTimeout(1000);
                    while (flag) {
                        try {
                            ElementHandle obj = page.waitForSelector(".rros-ui-lib-table__rows");//, new Page.WaitForSelectorOptions().setTimeout(3000)
                            if (obj != null) {
                                obj.click();
                            }
                            page.waitForTimeout(1000);
                            ElementHandle error = page.querySelector(".rros-ui-lib-errors");

                            if (error == null) {
                                System.out.println("nety");
                                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("kyyyy.png")));
                                flag = false;
                            }else {
                                System.out.println("est");
//                                    page.waitForTimeout(1000);
//                                    obj.click();
                                error.evaluate("element => element.remove()");
                                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("buttonClick.png")));
                                page.waitForTimeout(3000);
                                continue;
                            }
                        }catch (TimeoutError e){
                            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("timeout.png")));
                            System.out.println("Time out");
                            flag = true;
                        }

                    }

                    page.waitForLoadState(LoadState.NETWORKIDLE);

//                        page.waitForTimeout(3000);
                    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ky1.png")));

                    ElementHandle kagElem = page.waitForSelector("button[class='build-card-wrapper__header-wrapper__action-button " +
                            "rros-ui-lib-button rros-ui-lib-button--secondary']");
                    if (kagElem != null) {kagElem.click();}
                    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ky.png")));
//                        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ky.png")));
                    page.waitForLoadState(LoadState.NETWORKIDLE);
//                        List<ElementHandle> links = page.querySelectorAll(".rros-ui-lib-link_inherit");
//                        for (ElementHandle link : links) {
//                            if (link.innerText().equals("Запросить сведения об объекте")) {
//                                link.click();
//                                break;
//                            }
//                        }
                    page.click(".rros-ui-lib-link_inherit");
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                    page.waitForTimeout(5000);

                    sentLong++;
                } catch (PlaywrightException e) {
                    e.printStackTrace();
                    error++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            error++;
        }

        sent.setText("Отправлено: " + sentLong);
        notSent.setText("Не отправлено: " + error);
    }

    @FXML
    private void submitVerificationCode() {
        try {
//            System.out.println(page.content());
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("login.png")));

            page.fill("input[type='tel']", verifCode.getText());
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("login1.png")));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            labelAuthorization.setText("Авторизация успешна");

//            browserContext.storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get(contextState)));
        } catch (PlaywrightException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {

//        try(Playwright playwright = Playwright.create()) {
//
//            contextState = "contextState.json";
//
//            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
//
//            if(Paths.get(contextState).toFile().exists()) {
//
//                String contextStateJson = new String(Files.readAllBytes(Paths.get(contextState)));
//
//                browserContext = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true).setStorageState(contextStateJson));
//                labelAuthorization.setText("Автоматом авторизирован");
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        try {
            Playwright playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            browserContext = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
            page = browserContext.newPage();
        } catch (PlaywrightException e) {
            e.printStackTrace();
        }
    }

    public boolean checkId(String obj){
        String key = obj.replaceAll("\\D","");
        boolean flag = false;
        if(map.isEmpty()) {
            map.put(key, obj);
        }else {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getKey().equals(key)) {
                    flag = true;
                }
            }
            if(!flag){
                map.put(key, obj);
            }
        }
        System.out.println(map.size());
        return flag;
    }

    public void downloadFile(){

        long start = System.currentTimeMillis();
//        Page key = browserContext.newPage();
        page.navigate("https://lk.rosreestr.ru/my-keys");
        page.waitForLoadState(LoadState.NETWORKIDLE);

//        Page page = browserContext.newPage();
        page.navigate("https://lk.rosreestr.ru/request-access-egrn/my-claims");
        page.waitForLoadState(LoadState.NETWORKIDLE);

//            List<ElementHandle> list = page.querySelectorAll("a.rros-ui-lib-link.rros-ui-lib-link_medium.rros-ui-lib-link__color");
        List<ElementHandle> list = page.querySelectorAll("a:text('Скачать')");
        int counter = 0;

        boolean flag = true;
        while (flag) {
            System.out.println("list " + list.size());
            for (ElementHandle link : list) {
                System.out.println("Текст ссылки");

                boolean success = false;
                while (!success) {
                    try {

                        Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(20000), () -> {
                            link.click();
                            page.waitForLoadState(LoadState.NETWORKIDLE);
                        });
                        if(checkId(download.url()))
                            continue;

                        download.saveAs(Paths.get("Загрузки/", download.suggestedFilename() + "_" + counter));
                        counter++;
                        System.out.println("download " + download.url());
                        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("step.png")));
                        success = true;
                        System.out.println("Скачал");
                    } catch (Exception e) {
                        System.out.println("Попытка не удалась. Пробую снова...");
                    }
                }
            }
            ElementHandle elementHandle = page.querySelector("button.rros-ui-lib-table-pagination__btn.rros-ui-lib-table-pagination__btn--next[data-id='']");
            if(elementHandle.isVisible()) {
                elementHandle.click();//???
            }else
                flag = false;


        }
        long end = System.currentTimeMillis();
        System.out.print("time: " + (end - start) + "ms");

    }

}

