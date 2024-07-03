package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataParser {

    public Map<String,String> map;

    private File file;

    private long sent;
    private long downloaded;
    private long notSent;
    private final Page page;


    public DataParser(Page page){
        map = new HashMap<>();
        sent = 0;
        downloaded = 0;
        notSent = 0;
        this.page = page;
    }

    public long getSent(){
        return sent;
    }

    public long getLoad(){return downloaded;}

    public long getNotSent(){return notSent;}

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
//        System.out.println(map.size());
        return flag;
    }

    public boolean isFile(File file) {
        System.out.println();
        boolean flag = true;
        if(file == null) {
            return flag;
        }
        Path path = Paths.get(file.getAbsolutePath());
        String fileType = "";
        try {
            String mimeType = Files.probeContentType(path);
            System.out.println(mimeType);
            fileType = mimeType == null ? "" : mimeType;
        } catch (IOException e) {
//            throw new RuntimeException(e);
            return flag;
        }

        if(fileType.equals("text/plain") || file.getName().toLowerCase().endsWith(".txt")) {
            System.out.println("Файл загружен");
            this.file = file;
            flag = false;
        }else {
            flag = true;
        }
        return flag;
    }

    public void downloadData(){

        page.navigate("https://lk.rosreestr.ru/request-access-egrn/my-claims");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        
//        long start = System.currentTimeMillis();
        List<ElementHandle> list = page.querySelectorAll("a:text('Скачать')");
        int counter = 0;

        boolean flag = true;
        while (flag) {
//            System.out.println("list " + list.size());
            for (ElementHandle link : list) {
//                System.out.println("Текст ссылки");

                boolean success = false;
                while (!success) {
                    try {

                        Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(20000), () -> {
                            link.click();
                            page.waitForLoadState(LoadState.NETWORKIDLE);
                        });
                        if(checkId(download.url()))
                            continue;

                        download.saveAs(Paths.get( "Загрузки/" ,counter +"_"+download.suggestedFilename()));
                        counter++;
//                        System.out.println("download " + download.url());
//                        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("step.png")));
                        success = true;
                        System.out.println("Скачал");
                        downloaded++;

                    } catch (TimeoutError ignored) {}
                }
            }

            ElementHandle elementHandle = page.querySelector("button.rros-ui-lib-table-pagination__btn.rros-ui-lib-table-pagination__btn--next[data-id='']");
            if(elementHandle.isVisible()) {
                elementHandle.click();
//                System.out.println("Кнопка есть я нажал");
            }else {
                flag = false;
//                System.out.println("Кнопки нету");
            }

        }
        long end = System.currentTimeMillis();
//        System.out.print("time: " + (end - start)/1000 + "s");
    }


    public void addNumbers(){
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
        String line;
//        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("key1.png")));
        page.navigate("https://lk.rosreestr.ru/my-keys");
        page.waitForLoadState(LoadState.NETWORKIDLE);
//        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("key.png")));
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
//                                System.out.println("nety");
//                                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("kyyyy.png")));
                                flag = false;
                            }else {
//                                System.out.println("est");
//                                    page.waitForTimeout(1000);
//                                    obj.click();
                                error.evaluate("element => element.remove()");
//                                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("buttonClick.png")));
                                page.waitForTimeout(3000);
                                continue;
                            }
                        }catch (TimeoutError e){
//                            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("timeout.png")));
                            System.out.println("Time out");
                            flag = true;
                        }

                    }

                    page.waitForLoadState(LoadState.NETWORKIDLE);
//                    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ky1.png")));

                    ElementHandle kagElem = page.waitForSelector("button[class='build-card-wrapper__header-wrapper__action-button " +
                            "rros-ui-lib-button rros-ui-lib-button--secondary']");
                    if (kagElem != null) {kagElem.click();}
//                    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ky.png")));
                    page.waitForLoadState(LoadState.NETWORKIDLE);

                    page.click(".rros-ui-lib-link_inherit");
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                    page.waitForTimeout(5000);

                    sent++;
                } catch (PlaywrightException e) {
                    e.printStackTrace();
                    notSent++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            notSent++;
        }
    }


}
