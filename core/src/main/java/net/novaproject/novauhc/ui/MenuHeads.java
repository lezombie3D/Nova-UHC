package net.novaproject.novauhc.ui;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.DyeColor;

public final class MenuHeads {

    public enum Shape { BACK, LEFT, RIGHT, PLUS, MINUS, DENY, ACCEPT }

    private static final Map<DyeColor, String[]> BY_COLOR = new EnumMap<>(DyeColor.class);

    static {
        put(DyeColor.WHITE,
                "ddf29faa67ae5b613dc654da4331b98733da9734a762c8ee91ba146b6341",
                "cdc9e4dcfa4221a1fadc1b5b2b11d8beeb57879af1c42362142bae1edd5",
                "956a3618459e43b287b22b7e235ec699594546c6fcd6dc84bfca4cf30ab9311",
                "60b55f74681c68283a1c1ce51f1c83b52e2971c91ee34efcb598df3990a7e7",
                "c3e4b533e4ba2dff7c0fa90f67e8bef36428b6cb06c45262631b0b25db85b",
                "1d1a3c96562348527d5798f291609281f72e16d611f1a76c0fa7abe043665",
                "ee28bea9d39373d36ee8fa40ec83f9c3fcdd93175227743f9dd1f7e7886b7ee5");
        put(DyeColor.ORANGE,
                "1457df509d865ca67d29bd5b42312a3365246f2edbef1c1fd7b8974b25dca2",
                "6e8c3ce2aee6cf2faade7db37bbae73a36627ac1473fef75b410a0af97659f",
                "6e8cd53664d9307b6869b9abbae2b7737ab762bb18bb34f31c5ca8f3edb63b6",
                "5250b3cce76635ef4c7a88b2c597bd2749868d78f5afa566157c2612ae4120",
                "acff91dc99d5828023eedf873799d25535dade64a2e16a3b498b4113eafd4966",
                "7fe1d222eb1a35d733fbde7c1342efa821898b56c7c28a69b6878920b511",
                "4c4d5461361b99793ea1c0498b807d6613b7b2c46a7a77b3d6d602be92ace343");
        put(DyeColor.MAGENTA,
                "6c75d5e2595bf928b544cd945bd6478fe39bf5b7c631ee3a2ff7724b4477f3",
                "9dbd8ec46192a955c6bd651ab4d86f9867e4d4bd99af21ca92ec9d26f86da91",
                "8078e16892a2c9c02305213cce60483ab7ada47f13af9b8da87ce63dd8347",
                "11715becc4b7eb1768d4a0376f7759ffbcc57909f8fd414d05a4094b6b3abf0",
                "e62e124f3c457b4061cd87a37198b2fbebdbe5b044fa6cce45e7d82ab25df471",
                "f9982618aa5c8b94372a16c6662cb2f3e977412e701ec6c2376ccf39fddbb",
                "77420f8cc510ebdcc9becf60b3500627fd715a32305088907824710604b87d56");
        put(DyeColor.LIGHT_BLUE,
                "42246c8abe7f3732cef758379353a28815a9c3707b321295731e2215abaf6faf",
                "93971124be89ac7dc9c929fe9b6efa7a07ce37ce1da2df691bf8663467477c7",
                "2671c4c04337c38a5c7f31a5c751f991e96c03df730cdbee99320655c19d",
                "bd86db9a14d5879fa2811d301ccbd526994f871247b62f2d9a48183e9641ad69",
                "10c34d5e85cf3aa31641993445fad6f5f1e01098e75caa56d9ae754026ca69",
                "fc68a342df6c9e711ec3bc138f91cdb1b0fa5e72f66e4251875d8bedd0553eb5",
                "3e1f0846c10e725da79eded6a01e8df6da0fd1196ed349927c646838269b5f37");
        put(DyeColor.YELLOW,
                "953bda2ed46efae26d6b6f3282e28394ee39f3313cf39af8d9e39709b87643b",
                "49b2bee39b6ef47e182d6f1dca9dea842fcd68bda9bacc6a6d66a8dcdf3ec",
                "141ff6bc67a481232d2e669e43c4f087f9d2306665b4f829fb86892d13b70ca",
                "edfff1b3c5d85fe3cdd5656869baa0eade5e53aca9d561427648cc72f5e25a9",
                "ed24dfaf1ee17d73eeec2422158ca33ad187ee727abb796f2132dedfd01fc49e",
                "6360dcf8235f823f6cbb2a996457daa7b92b5ef1554ab58e0f34839118d54",
                "eef425b4db7d62b200e89c013e421a9e110bfb27f2d8b9f5884d10104d00f4f4");
        put(DyeColor.LIME,
                "f1fc97a7a286f69c8f9ae596113a5797d59accb253155451dbd3ced1c35c44",
                "f5347423ee55daa7923668fca8581985ff5389a45435321efad537af23d",
                "4ef356ad2aa7b1678aecb88290e5fa5a3427e5e456ff42fb515690c67517b8",
                "b056bc1244fcff99344f12aba42ac23fee6ef6e3351d27d273c1572531f",
                "5f67f77f9697255553fa17979cf7242ce7d75dd52fbf3cfb26c9ab54e2a71938",
                "caba2d5a5ca842a2da7d25a259e77b6cba5ae655dddc24ea495641d29efc",
                "a92e31ffb59c90ab08fc9dc1fe26802035a3a47c42fee63423bcdb4262ecb9b6");
        put(DyeColor.PINK,
                "b89d3b9fd93ba76fa395dea9122d508bdad2bbbafe251c9fc41b977f526cc0",
                "42ee597ab5bde98a7539625b414c87db386ff7f9d526ff9839e99696308b4a9e",
                "186673dc1eaa6ac275c8add1e4c543ef94d93fae71dbf6c046f056e7f344aaaf",
                "f3bfe4131a6f612c75b45d80839bcb37edd7e8717e695a3e64ce9d033beafe6",
                "c54305813828323cab2eda48f692b112556d89d4bcedda84b5c2fe3611ab8",
                "9f7455dae9698823e4087209cc8e764319ebc3348f7fabaa9a815594fc64e",
                "28fc87aa84cc33fb5fbfaa62af69d6ee69b26498e4444c9ac94f623e4f9fcdb3");
        put(DyeColor.GRAY,
                "56961ad1f5c76e97358c444fe0e83a39564e6b48109170984a84eca5dccd424",
                "86971dd881dbaf4fd6bcaa93614493c612f869641ed59d1c9363a3666a5fa6",
                "f32ca66056b72863e98f7f32bd7d94c7a0d796af691c9ac3a9136331352288f9",
                "10c97e4b68aaaae8472e341b1d872b93b36d4eb6ea89ecec26a66e6c4e178",
                "c674b682b179d1d4fa791f92cdaf1c2f37fad4eadbc7dbdc060e8154d351482",
                "bcc5fae3b650e6edb3245fa41e5be2da79b0e17f22c4b4e1e59b32f573202d18",
                "85a3755a6fe019a173ce3a43070452e767768d57559d04b73e21b903eaa1bd82");
        put(DyeColor.SILVER,
                "11a68d5b87fba62476808a0d9a75c3ddf1bc26c0b0f4d2de36c78843af129",
                "542fde8b82e8c1b8c22b22679983fe35cb76a79778429bdadabc397fd15061",
                "406262af1d5f414c597055c22e39cce148e5edbec45559a2d6b88c8d67b92ea6",
                "632fff163e235632f4047f4841592d46f85cbbfde89fc3df68771bff69a662",
                "da56dab53d4ea1a79a8e5ed6322c2d56cb714dd35edf478763ad1a84a8310",
                "f5f3ea7c7b26a054a9fbbb28b97a608999c2c73df75bf6b2348d7fb1e59e855",
                "6fffa602e368214dd62cecd6815f14926ee57b948143495e931a77636372be5a");
        put(DyeColor.CYAN,
                "fc3be6b132012a0c2e062edeb251c5b5c14a3c93cfeca89250963c9cbd56ab",
                "6768edc28853c4244dbc6eeb63bd49ed568ca22a852a0a578b2f2f9fabe70",
                "6ff55f1b32c3435ac1ab3e5e535c50b527285da716e54fe701c9b59352afc1c",
                "dd8b7173d841f2563ec108888b0f797917efc18be27861f0a6761aa3ed91ce",
                "c67836f047d741ac4f7385f7c3dc48ba8df43732e5aeec3326829a7284737",
                "7c68deb8bc576b4d631eadde89dc9cd946a3bb7e347b8c4da5d82c888ed15c3c",
                "8c813be108b31157fbb9b50d836caa0d2612f6bcfef4ae0c6b77ae3479e02a3e");
        put(DyeColor.PURPLE,
                "ec8de5f42dbc19eadf1e713d81d54899b379cf653ab3dc41303fbfdbf76398",
                "2eff7559102aa8ff9712976b62292ee45e0793c46f668916868207f399effab",
                "f726d288a74e8ac924c58c93f5b35cacd6946a6ccfdbda85fe8acb9e3eecebdb",
                "7fce66789b77b261e669b239fcd4a6296965585ad01933f830de7a93de4374ce",
                "f9710ceae69b72a177e5abbb86c8ada432981ce7fc2d3ed1c651560f7a11e",
                "4b48ca8f92c15ff5ef3801cde3dde8f4efead877a60a28b72ae17241896d",
                "d5d4c645eb42bee415658e1661889e62268b5cb3f1e1da33ac6cc1e00a7090ff");
        put(DyeColor.BLUE,
                "288eb5f528cec583803757ba202c15362f84a3f6fbe8712e8ab256f25e5245",
                "5ae78451bf26cf49fd5f54cd8f2b37cd25c92e5ca76298b3634cb541e9ad89",
                "117f3666d3cedfae57778c78230d480c719fd5f65ffa2ad3255385e433b86e",
                "eaea5d7127214ee2cca1b82ef82f8ae55929325766ea84dde18c11de0c7d591",
                "f84ccff09f83a2aed9ebe08b6817eec667fb996d12986cb98ede41af58bb1d",
                "d5e1be33374fd7b2bae9c8fc9146b6ed3eedcb1476b3b7b8010f5f44bfa843e",
                "a6f4431e983b3c854044bc00b7c54800243cd7dbf94de339b8c2f7b2566a5201");
        put(DyeColor.BROWN,
                "c123fbbff27bbf98ca6840b890dfb4667f0925d3ba91d853dfa26ba12d1d961",
                "5d4b334dc51156a8d8abc42c84b32c9ce8763c4204ca573bbd09fd037147b",
                "b2825bca38e9a22edf5ec9e096a3f68f59ece8e4ec471f7cdca337096fefa83",
                "ccb3b9ffc5334eb6249c9964605e14e3692bd6b175d2a963ca317c7958ddb2",
                "b78666eaf42bfa6eec564a3dd7f986546da12c4ee0e5e72fbaa2c5aeb08f9c",
                "41db2b5439101b95c6807053a1d69d8dffa7c4b8049c8810178fc4ba37a",
                "8b1e9ae370f1a3ed575e6174569de448f2aff3f2c6d28678d26560f5e9ee3209");
        put(DyeColor.GREEN,
                "33658f9ed2145ef8323ec8dc2688197c58964510f3d29939238ce1b6e45af0ff",
                "8550b7f74e9ed7633aa274ea30cc3d2e87abb36d4d1f4ca608cd44590cce0b",
                "96339ff2e5342ba18bdc48a99cca65d123ce781d878272f9d964ead3b8ad370",
                "5ff31431d64587ff6ef98c0675810681f8c13bf96f51d9cb07ed7852b2ffd1",
                "cd15b5d79bedadc145fa17cc36a2e0c9b088b4ee74f86ea588bd1ad8cdbd22",
                "6e8646bb76f0c6d03a637a9b4975b3a85276eb20d6f16bdb91866c8c3c8285c5",
                "4312ca4632def5ffaf2eb0d9d7cc7b55a50c4e3920d90372aab140781f5dfbc4");
        put(DyeColor.RED,
                "a9dbed522e8de1a681dddd37854ee4267efc48b59917f9a9acb420d6fdb9",
                "f84f597131bbe25dc058af888cb29831f79599bc67c95c802925ce4afba332fc",
                "fcfe8845a8d5e635fb87728ccc93895d42b4fc2e6a53f1ba78c845225822",
                "ac731c3c723f67d2cfb1a1192b947086fba32aea472d347a5ed5d7642f73b",
                "4e4b8b8d2362c864e062301487d94d3272a6b570afbf80c2c5b148c954579d46",
                "beb588b21a6f98ad1ff4e085c552dcb050efc9cab427f46048f18fc803475f7",
                "ff9d9de62ecae9b798555fd23e8ca35e2605291939c1862fe79066698c9508a7");
        put(DyeColor.BLACK,
                "f1fab0e6aea88748ca3b5512ed502a6d18e76d8afc4770d995233ac0dc5186",
                "37aee9a75bf0df7897183015cca0b2a7d755c63388ff01752d5f4419fc645",
                "682ad1b9cb4dd21259c0d75aa315ff389c3cef752be3949338164bac84a96e",
                "9a2d891c6ae9f6baa040d736ab84d48344bb6b70d7f1a280dd12cbac4d777",
                "935e4e26eafc11b52c11668e1d6634e7d1d0d21c411cb085f9394268eb4cdfba",
                "c38ab145747b4bd09ce0354354948ce69ff6f41d9e098c6848b80e187e919",
                "2b0c1647dae5d5f6bc5dca549f1652556c7f1bc08adee37ccfc4090bc20e647e");
    }

    private static String tex(String hash) {
        return Base64.getEncoder().encodeToString(
                ("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + hash + "\"}}}")
                        .getBytes(StandardCharsets.UTF_8));
    }

    private static void put(DyeColor color, String back, String left, String right,
                            String plus, String minus, String deny, String accept) {
        BY_COLOR.put(color, new String[]{tex(back), tex(left), tex(right), tex(plus), tex(minus), tex(deny), tex(accept)});
    }

    public static String of(Shape shape, DyeColor color) {
        String[] set = BY_COLOR.get(color);
        if (set == null) set = BY_COLOR.get(DyeColor.WHITE);
        return set[shape.ordinal()];
    }

    public static final String HEADER_SCENARIOS = tex("d11e997d0f18b6a295a4bf0bd7aafcf16edad832107d2ff271c781d56dd91a73");
    public static final String GAMEMODE = tex("559b83b93d4918cee103a1ed57bc2ced03b47547512c1f7ea181eff560b98032");
    public static final String WORLD = tex("bdde594dead88b35bc21ad1ab238dcae411253e34a585d925258ce674c642617");
    public static final String QUESTION = tex("bc8ea1f51f253ff5142ca11ae45193a4ad8c3ab5e9c6eec8ba7a4fcb7bac40");
}
