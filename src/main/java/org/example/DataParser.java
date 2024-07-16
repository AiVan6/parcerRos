package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
        if(map.containsKey(key)) {
            return true;
        }
        else {
            map.put(key,obj);
            return false;
        }
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

    public void downloadData() {
        page.navigate("https://lk.rosreestr.ru/request-access-egrn/my-claims");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        final int[] counter = {0};

        boolean flag = true;
        while (flag) {
            List<ElementHandle> list = page.querySelectorAll("a:text('Скачать')");
//            System.out.println("list size: " + list.size());

            if(!list.isEmpty()) {

                for (ElementHandle link : list) {
                    CompletableFuture<Void> thread = CompletableFuture.runAsync(() -> {
                        boolean success = false;
                        while (!success) {
                            try {
                                Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(60000), () -> {
                                    link.click();
//                                    System.out.println("link: " + link);
                                });

                                if (checkId(download.url())) {
                                    continue;
                                }

                                download.saveAs(Paths.get("Загрузки/", counter[0] + "_" + download.suggestedFilename()));
                                success = true;
                                System.out.println("Скачал");
                                counter[0]++;
                            } catch (TimeoutError ignored) {
                                System.out.println("Не скачал");
                            }
                        }
                    });
//                    threads.add(thread);
//                    thread.start();
                    try {
                        thread.get();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }


                // Check if there is a next page
                ElementHandle nextButton = page.querySelector("button.rros-ui-lib-table-pagination__btn.rros-ui-lib-table-pagination__btn--next[data-id='']");
                if (nextButton != null && nextButton.isVisible()) {
                    nextButton.click();
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                } else {
                    flag = false;
                }
            }else {
                System.out.println("На пути попалась капча и я не смог её решить");
                flag=false;
                System.out.println("Решите капчу на сайте и перезапустите меня");
            }
        }
    }



    public void addNumbers(){
        BufferedReader reader = null;
        FileWriter writer = null;

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
            writer = new FileWriter(new File("notSent.txt"));

            while ((line = reader.readLine()) != null) {
                try {
                    boolean flag = true;
                    page.navigate("https://lk.rosreestr.ru/request-access-egrn/property-search");
                    page.waitForLoadState(LoadState.NETWORKIDLE);

                    page.fill("#react-select-3-input",line);

                    page.waitForLoadState(LoadState.NETWORKIDLE);

                        page.waitForTimeout(10000);
//                    while (flag) {
                    ElementHandle obj = page.waitForSelector(".rros-ui-lib-table__rows");//, new Page.WaitForSelectorOptions().setTimeout(3000)
                    if (obj != null) {
                        obj.click();
                    }
                    page.waitForTimeout(5000);
//                    ElementHandle error = page.querySelector(".rros-ui-lib-errors");
                        

//                    page.waitForLoadState(LoadState.NETWORKIDLE);
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
//                    e.printStackTrace();
                    System.out.println("Не добавил");
                    notSent++;
                    System.out.println(line);
                    writer.write(line);
//                    bufferedWriter.newLine();
                    page.waitForTimeout(3000);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            notSent++;
        }
        finally {
            try {
                if(reader != null) {
                    reader.close();
                }
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }


}
