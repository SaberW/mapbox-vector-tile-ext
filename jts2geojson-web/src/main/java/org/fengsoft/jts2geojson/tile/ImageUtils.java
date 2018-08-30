package org.fengsoft.jts2geojson.tile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author JerFer
 * @Date 2018/8/30---11:19
 */
public class ImageUtils {
    //    private void convert2MultiFile() {
//        //sprite json文件
//        String text = ReadFile(textBox3.Text);
//        JObject obj = JObject.Parse(text);
//        JToken item = null;
//        //将json转为对象
//        List<Param> paramlist = new List<Param>();
//        for (int i = 0; i < obj.Count; i++) {
//            if (item == null) {
//                item = obj.First;
//            } else {
//                item = item.Next;
//            }
//            Param p = new Param();
//            p.name = item.Path.Substring(2, item.Path.Length - 4).Replace("/", "-").Replace(":", "&");
//            p.x = (int) item.First["x"];
//            p.y = (int) item.First["y"];
//            p.width = (int) item.First["width"];
//            p.height = (int) item.First["height"];
//            paramlist.Add(p);
//        }
//        using(Bitmap map = (Bitmap) Image.FromFile(textBox3.Text + @ "\sprite.png"))
//        {
//            using(Bitmap editMap = new Bitmap(map, map.Width, map.Height))
//            {
//                foreach(var itemp in paramlist)
//                {
//                    //保存图片的画布
//                    Bitmap itemMap = new Bitmap(itemp.width, itemp.height);
//                    for (int i = 0; i < itemp.width; i++) {
//                        for (int j = 0; j < itemp.height; j++) {
//                            //获取像素
//                            Color color = editMap.GetPixel(itemp.x + i, itemp.y + j);
//                            itemMap.SetPixel(i, j, color);
//                        }
//                    }
//                    //保存
//                    String savepath = System.Environment.CurrentDirectory + @ "\spriteicon" + itemp.name + ".png";
//                    itemMap.Save(savepath);
//                }
//            }
//        }
//    }
//
//    private void imageResize() {
//        DirectoryInfo folder = new DirectoryInfo(System.Environment.CurrentDirectory);
//        List<String> filenames = new List<String>();
//        int addnum = Convert.ToInt32(textBox2.Text);
//        foreach(var NextFolder in folder.GetFiles("*.png"))
//        {
//            if (NextFolder.Name.Contains(textBox1.Text)) {
//                filenames.Add(NextFolder.Name);
//            }
//        }
//        foreach(var item in filenames)
//        {
//            using(Bitmap map = (Bitmap) Image.FromFile(System.Environment.CurrentDirectory + "/" + item))
//            {
//                using(Bitmap editMap = new Bitmap(map.Width + addnum, map.Height))
//                {
//                    int centernum = map.Width / 2;
//                    for (int i = 0; i < map.Width; i++) {
//                        for (int j = 0; j < map.Height; j++) {
//                            //获取像素
//                            Color color = map.GetPixel(i, j);
//                            if (i == centernum) {
//                                editMap.SetPixel(i, j, color);
//                                if (addnum > 0) {
//                                    for (int m = 0; m < addnum; m++) {
//                                        editMap.SetPixel(i + m + 1, j, color);
//                                    }
//                                }
//                            } else if (i < centernum) {
//                                editMap.SetPixel(i, j, color);
//                            } else {
//                                editMap.SetPixel(i + addnum, j, color);
//                            }
//                        }
//                    }
//                    //保存
//                    String savepath = System.Environment.CurrentDirectory + @ "\result\" + item;
//                    editMap.Save(savepath);
//                }
//            }
//        }
//    }
    public static void main(String[] args) throws IOException {
        convert2SingleFile("E:\\Data\\style\\resize-grey\\2018\\img\\grey");
    }

    private static void convert2SingleFile(String imagePath) throws IOException {
        File file = new File(imagePath);
        File[] pngFiles = file.listFiles((dir, name) -> name.toLowerCase().endsWith("png"));
        List<Param> paramlist = new ArrayList<>();
        for (File pngFile : pngFiles) {
            try {
                BufferedImage bufferedImage = ImageIO.read(pngFile);
                Param p = new Param();
                p.setName(pngFile.getName().substring(0, pngFile.getName().lastIndexOf('.')));
                p.setWidth(bufferedImage.getWidth());
                p.setHeight(bufferedImage.getHeight());
                paramlist.add(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //图片默认宽度为255，
        int widthnum = 255;
        paramlist.stream().sorted((a, b) -> {
            if (a.getName().length() < b.getName().length()) return 1;
            else return -1;
        }).sorted((a, b) -> {
            if (a.getHeight() > b.getHeight()) return 1;
            else return -1;
        });
        //一行一行的图片集合
        List<List<Param>> rowparams = new ArrayList<>();
        List<Param> paramnowlist = new ArrayList<>();
        int countnum = 0;
        for (int i = 0; i < paramlist.size(); i++) {
            countnum += paramlist.get(i).getWidth();
            if (countnum > widthnum) {
                i = i - 1;
                countnum = 0;
                rowparams.add(paramnowlist);
                paramnowlist = new ArrayList<>();
            } else {
                paramnowlist.add(paramlist.get(i));
            }
            if (i == paramlist.size() - 1) {
                rowparams.add(paramnowlist);
                break;
            }
        }
        //计算应有的高度
        int allheight = 0;
        for (List<Param> item : rowparams) {
            allheight += item.stream().max((a, b) -> {
                if (a.getHeight() > b.getHeight()) return a.getHeight();
                return b.getHeight();
            }).get().getHeight();
        }
        String spritejson = "{";
        BufferedImage editMap = new BufferedImage(widthnum, allheight, BufferedImage.TYPE_4BYTE_ABGR);
        //开始画大图
        //保存起始高度
        int heighttemp = 0;
        for (int i = 0; i < rowparams.size(); i++) {
            int tempwidthnum = 0;
            for (int j = 0; j < rowparams.get(i).size(); j++) {
                BufferedImage map = ImageIO.read(new File(imagePath + File.separator + rowparams.get(i).get(j).getName() + ".png"));
                //循环小图片
                for (int x = 0; x < map.getWidth(); x++) {
                    for (int y = 0; y < map.getHeight(); y++) {
                        //获取像素
                        editMap.setRGB(x + tempwidthnum, y + heighttemp, map.getRGB(x, y));
                    }
                }

                spritejson += "\"" + rowparams.get(i).get(j).getName().replace("-", "/").replace("&", ":") + "\":{\"x\":";
                spritejson += tempwidthnum + ",\"y\":" + heighttemp + ",\"width\":" + rowparams.get(i).get(j).getWidth();
                spritejson += ",\"height\":" + rowparams.get(i).get(j).getHeight() + ",\"pixelRatio\":1,\"sdf\":false},";
                //增加宽度
                tempwidthnum += rowparams.get(i).get(j).getWidth();
            }
            heighttemp += rowparams.get(i).stream().max((a, b) -> {
                if (a.getHeight() > b.getHeight()) return a.getHeight();
                return b.getHeight();
            }).get().getHeight();
        }
        //保存大图
        String savepath = imagePath + File.separator + "sprite.png";
        ImageIO.write(editMap, "png", new File(savepath));

        spritejson = spritejson.substring(0, spritejson.lastIndexOf(","));
        spritejson += "}";
        //写入文件
        FileWriter fileWriter = new FileWriter(imagePath + File.separator + "sprite.json");
        fileWriter.write(spritejson);
        fileWriter.close();
    }
}
