package com.example.data

import com.example.model.LevelRule
import com.example.model.VisualPreviewType

object LevelRuleCatalog {

    val MILESTONE_LEVELS = listOf(1, 4, 11, 21, 31, 41, 51, 61, 71, 81, 91, 100)

    fun isMilestone(levelId: Int): Boolean = levelId in MILESTONE_LEVELS

    fun getTierForLevel(levelId: Int): Int = when (levelId.coerceIn(1, 100)) {
        in 1..3 -> 1
        in 4..10 -> 2
        in 11..20 -> 3
        in 21..30 -> 4
        in 31..40 -> 5
        in 41..50 -> 6
        in 51..60 -> 7
        in 61..70 -> 8
        in 71..80 -> 9
        in 81..90 -> 10
        in 91..99 -> 11
        else -> 12
    }

    fun getRuleForLevel(levelId: Int, isTurkish: Boolean = false): LevelRule {
        val id = levelId.coerceIn(1, 100)
        val tier = getTierForLevel(id)
        val isMajor = isMilestone(id)

        return if (isTurkish) {
            buildTurkishRule(id, tier, isMajor)
        } else {
            buildEnglishRule(id, tier, isMajor)
        }
    }

    private fun buildTurkishRule(id: Int, tier: Int, isMajor: Boolean): LevelRule {
        return when (tier) {
            1 -> { // Levels 1..3
                val (title, sub, rule, hazard, tip, icon) = when (id) {
                    1 -> Tuple6(
                        "Tek Çizgi Akışı",
                        "Milat: Bölüm 1 • Başlangıç",
                        "Karakter daima 1 numaralı düğümden başlar. Parmağını ekrandan kaldırmadan tüm düğümleri tek ve kesintisiz bir rotada birleştir!",
                        "Parmağını ekrandan kaldırmak, 1 harici noktadan başlamak veya önceki düğüme geri dönmek kırmızı bir Yankı (+1) oluşturur.",
                        "Çizim yapmadan önce 5 saniyelik önizlemede rotayı gözünle planla. 0 Hata ile tamamlarsan Kusursuz Hayalet açılır.",
                        "🎯"
                    )
                    2 -> Tuple6(
                        "Yankı İzi & Geçmiş Hatası",
                        "Harmonik Başlangıç • Bölüm 2",
                        "Yaptığın hatalar kaybolmaz; kırmızı bir 'Yankı Hattı' olarak ekranda kalır. Bu kırmızı hatlar artık fiziksel birer engele dönüşür.",
                        "Kendi oluşturduğun kırmızı yankı hatlarına çarparsan yeni bir yankı tetiklenir ve tahtada deadlock riski artar.",
                        "Işınlar birbirinin içinden ve yankılardan geçebilir; ancak eski kırmızı hatlara geri çarpmamaya özen göster.",
                        "👣"
                    )
                    else -> Tuple6(
                        "Yaşayan Ağ Esnekliği",
                        "Harmonik Başlangıç • Bölüm 3",
                        "Ağ artık parmağının hareketine göre elastik olarak esner ve nefes alır. Düğümler arası gerilim dokunma hızına tepki verir.",
                        "Hızlı ani sıçramalar ağın titremesine ve hedef düğmenin kaçırılmasına yol açabilir.",
                        "Akıcı ve sakin sürüklemeler yap. Parmağını hedefe doğru kaydırırken living web salınımını hisset.",
                        "🕸️"
                    )
                }
                LevelRule(
                    levelId = id,
                    milestoneId = 1,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Öğretici: Temel Yaşayan Ağ",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = icon,
                    badgeColor = 0xFF0284C7,
                    accentColor = 0xFF38BDF8,
                    headline = "Temel Kural: Tek Hamlede Kusursuz Hat",
                    summary = "Tüm düğümleri tek kesintisiz rotada birleştir, yankılardan kaçın.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.PULSING_LIVING_WEB,
                    ruleKey = "CONTINUOUS_FLOW"
                )
            }
            2 -> { // Levels 4..10
                val title = if (id == 4) "Karmaşık Ağ Kafesi" else if (id == 7) "Esnek Köprü Hattı" else "Rezonans Matrisi #$id"
                val sub = "Milat: Bölüm 4 • Kademe II (4 - 10)"
                val rule = "Ağ genişliyor! Teller birbirine daha sıkı bağlanıyor ve çizim yaptıkça eski ışınlar canlı bir rezonansla titreşiyor."
                val hazard = "Karmaşık düğüm yoğunluğunda yanlış bir düğüme sapmak veya rotayı erken kesmek +1 Yankı yazar."
                val tip = "Düğümler arası ferah mesafeleri kullan; merkezdeki düğümleri erken tamamlayarak dış halkaya rahatça geç."
                LevelRule(
                    levelId = id,
                    milestoneId = 4,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Karmaşık Ağ",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🕸️",
                    badgeColor = 0xFF0EA5E9,
                    accentColor = 0xFF7DD3FC,
                    headline = "Kural: Sıkı Doku & Rezonans Titreşimleri",
                    summary = "Genişleyen ağda titreşen ışınları izle, rotanı önceden planla.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.COMPLEX_LATTICE,
                    ruleKey = "COMPLEX_WEB"
                )
            }
            3 -> { // Levels 11..20
                val title = if (id == 11) "Hareketli Düğümler" else if (id == 15) "Yörüngesel Salınım Ritmi" else "Kozmik Akıntı #$id"
                val sub = "Milat: Bölüm 11 • Kademe III (11 - 20)"
                val rule = "Düğümler artık sabit durmuyor! Kozmik akıntıyla hafifçe yörüngesel olarak hareket ediyor ve süzülüyorlar. Ezberlemek yetmez; hareketin ritmine uyum sağlamalısın."
                val hazard = "Düğüm hareket halindeyken yanlış noktaya sürüklemek veya kaçırmak rotayı kilitler ve +1 Yankı üretir."
                val tip = "Düğümlerin hareket salınımını izle; tepe (apex) noktalarında yavaşladıkları anda bağlantıyı kur!"
                LevelRule(
                    levelId = id,
                    milestoneId = 11,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Hareketli Düğümler",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "⚡",
                    badgeColor = 0xFF8B5CF6,
                    accentColor = 0xFFA78BFA,
                    headline = "Kural: Dinamik Yörüngeler & Hareketli Noktalar",
                    summary = "Sabit olmayan, süzülen düğümlerin ritmini yakalayarak çizim yap.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.ORBITAL_SHIFTING_NODES,
                    ruleKey = "SHIFTING_NODES"
                )
            }
            4 -> { // Levels 21..30
                val title = if (id == 21) "Görünmez Işınlar" else if (id == 25) "Kırpışan Pulsar Hatları" else "Gizli Dalga Boyu #$id"
                val sub = "Milat: Bölüm 21 • Kademe IV (21 - 30)"
                val rule = "Bağladığın ışınlar ve yollar çizildikten kısa süre sonra görünmezliğe gömülür! Çizgi fiziken oradadır ancak gizlenir. Rotanı zihninde canlandırmalısın."
                val hazard = "Görünmez olan bir hatta geri çarpmak veya daha önce geçtiğin yoldan habersizce dönmek +1 Yankı doğurur."
                val tip = "Işınlar gözden kaybolsa da tahtadaki boşlukları hafızanda haritalandır; geometrik şekli aklında tut."
                LevelRule(
                    levelId = id,
                    milestoneId = 21,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Görünmez Işınlar",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🌌",
                    badgeColor = 0xFFD97706,
                    accentColor = 0xFFFBBF24,
                    headline = "Kural: Görünmez Işınlar & Hafıza Testi",
                    summary = "Çizilen hatlar zamanla görünmez olur, zihinsel haritana güven.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.INVISIBLE_LINES_FADE,
                    ruleKey = "INVISIBLE_LINES"
                )
            }
            5 -> { // Levels 31..40
                val title = if (id == 31) "Zamanla Kaybolan Düğümler" else if (id == 35) "Zamansal Çözünme Akını" else "Erime Halkası #$id"
                val sub = "Milat: Bölüm 31 • Kademe V (31 - 40)"
                val rule = "Ağdaki bazı düğümler zaman geçtikçe enerjisini kaybeder ve etraflarındaki halka hızla erir! Bu düğümlere tükenmeden önce ulaşmalısın."
                val hazard = "Bir düğümün enerjisi tamamen biterse bağlantı kalıcı olarak çöker ve bölüm kilitlenir (+1 Yankı)."
                val tip = "Geri sayım halkası kırmızıya dönen düğümleri rotanın ilk sıralarına al; onları erkenden kurtar."
                LevelRule(
                    levelId = id,
                    milestoneId = 31,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Zamanla Kaybolan Düğümler",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "⏳",
                    badgeColor = 0xFFEC4899,
                    accentColor = 0xFFF472B6,
                    headline = "Kural: Zamansal Çözünme & Hızlı Kurtarma",
                    summary = "Solan düğümlerin enerjisi bitmeden rotaya dahil et.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.DECAYING_RING,
                    ruleKey = "DECAYING_NODES"
                )
            }
            6 -> { // Levels 41..50
                val title = if (id == 41) "Yankıların Hareketi" else if (id == 45) "Gezgin Geçmiş & Canavar Hırıltısı" else "Süzülen Yankı #$id"
                val sub = "Milat: Bölüm 41 • Kademe VI (41 - 50)"
                val rule = "Kırmızı yankı bariyerleri sabit kalmıyor! Uzay boşluğunda yavaşça süzülerek yön değiştiriyorlar. Hata yaptıkça Yankı Canavarı derin uykusundan uyanır."
                val hazard = "Süzülen kırmızı bir yankı parmağının çizdiği aktif hatta çarparsa anında yeni bir hata yankısı doğar."
                val tip = "Yankıların süzülme vektörünü takip et; iki bariyerin birbirinden uzaklaştığı boşluk anında aralarından geç."
                LevelRule(
                    levelId = id,
                    milestoneId = 41,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Yankıların Hareketi",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "👻",
                    badgeColor = 0xFF6366F1,
                    accentColor = 0xFF818CF8,
                    headline = "Kural: Süzülen Tehlikeler & Gezgin Yankılar",
                    summary = "Kırmızı yankılar hareket eder, Canavar uyanırken rotanı dikkatle koru.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.WANDERING_ECHO_DRIFT,
                    ruleKey = "WANDERING_ECHOES"
                )
            }
            7 -> { // Levels 51..60
                val title = if (id == 51) "Sahte Hedefler" else if (id == 55) "Kozmik İllüzyon Mayını" else "Serap Düğümü #$id"
                val sub = "Milat: Bölüm 51 • Kademe VII (51 - 60)"
                val rule = "Ağda aldatıcı illüzyon düğümleri (Decoy) belirdi! Gerçek düğümler canlı parıldarken, sahte hedefler kesik kesik titreşir."
                val hazard = "Sahte bir illüzyon düğümüne dokunursan hat çöker ve +1 Yankı cezası alırsın."
                val tip = "Gerçek hedeflerin halkaları kesintisiz dalgalanır ve tatlı harmonik tonlar verir; sahtelerden uzak dur."
                LevelRule(
                    levelId = id,
                    milestoneId = 51,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Sahte Hedefler",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🔮",
                    badgeColor = 0xFF14B8A6,
                    accentColor = 0xFF2DD4BF,
                    headline = "Kural: Sahte İllüzyonlar & Algı Sınavı",
                    summary = "Gerçek rezonansı sahte seraplardan ayırt et, hatasız ilerle.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.PHANTOM_DECOY,
                    ruleKey = "DECOY_TARGETS"
                )
            }
            8 -> { // Levels 61..70
                val title = if (id == 61) "Ters Yönler (Tek Yönlü Vektörler)" else if (id == 65) "Vektör Akış Tüneli" else "Karşıt Akıntı #$id"
                val sub = "Milat: Bölüm 61 • Kademe VIII (61 - 70)"
                val rule = "Vektör okları yolların yönünü kilitliyor! Belirli hatları yalnızca ok yönünde geçebilirsin. Ters yönden giriş yapılamaz."
                val hazard = "Vektör okunun gösterdiği yönün tersine çizim yapmaya çalışırsan akım engellenir ve +1 Yankı yazılır."
                val tip = "Bölüm başlamadan önce tek yönlü tünellerin girişlerini belirle; tünelin çıkışında kilitlenmeyecek bir sıra izle."
                LevelRule(
                    levelId = id,
                    milestoneId = 61,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Ters Yönler",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🔄",
                    badgeColor = 0xFF2563EB,
                    accentColor = 0xFF60A5FA,
                    headline = "Kural: Tek Yönlü Vektörler & Katı Akış",
                    summary = "Yalnızca ok yönünde ilerle, ters yöne gidişler kilitlidir.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.VECTOR_REVERSE_FLOW,
                    ruleKey = "REVERSE_FLOW"
                )
            }
            9 -> { // Levels 71..80
                val title = if (id == 71) "Ekranın Dönmesi" else if (id == 75) "Açısal Girdap Rotasyonu" else "Dönen Matris #$id"
                val sub = "Milat: Bölüm 71 • Kademe IX (71 - 80)"
                val rule = "Tüm oyun evreni ve koordinat ekseni yavaşça dönüyor! Parmağın ekranda kayarken tahtanın açısı sürekli değişecektir."
                val hazard = "Dönüş esnasında rotayı şaşırmak veya merkezkaç kaymasıyla yanlış koordinata dokunmak +1 Yankı üretir."
                val tip = "Dönüş merkezini referans al; parmağını tahtanın dönüş hızına paralel bir yay çizerek hareket ettir."
                LevelRule(
                    levelId = id,
                    milestoneId = 71,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Ekranın Dönmesi",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🌀",
                    badgeColor = 0xFFF59E0B,
                    accentColor = 0xFFFBBF24,
                    headline = "Kural: Açısal Rotasyon & Koordinat Dönüşü",
                    summary = "Dönen koordinat sisteminde merkezkaç kuvvetini yöneterek çiz.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.ROTATING_CANVAS,
                    ruleKey = "ROTATING_WEB"
                )
            }
            10 -> { // Levels 81..90
                val title = if (id == 81) "İki Ağ Aynı Anda" else if (id == 85) "Çift Boyutlu Kilit & Anahtar" else "Dolanık Portal #$id"
                val sub = "Milat: Bölüm 81 • Kademe X (81 - 90)"
                val rule = "İki paralel ağ katmanı (Alpha ve Beta) iç içe geçmiş durumda! Alpha boyutundaki anahtarı toplamadan Beta boyutundaki kilitli kapı açılamaz."
                val hazard = "Anahtarı almadan kilitli kapı düğümüne temas etmek rotayı kırar (+1 Yankı)."
                val tip = "İki ağ arasındaki köprü düğümleri kullanarak anahtara öncelik ver; kapıyı en son çıkış hedefin yap."
                LevelRule(
                    levelId = id,
                    milestoneId = 81,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "İki Ağ Aynı Anda",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "✨",
                    badgeColor = 0xFF8B5CF6,
                    accentColor = 0xFFC084FC,
                    headline = "Kural: Çift Boyutlu Dolanıklık & Kilitli Kapılar",
                    summary = "Anahtarı toplayıp kilitli kapıyı aç, iki boyutu birbirine bağla.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.DUAL_PORTAL,
                    ruleKey = "DUAL_ENTANGLED"
                )
            }
            11 -> { // Levels 91..99
                val title = if (id == 91) "Önceki Hataların Birleşmesi" else if (id == 95) "Canlı Hayalet Labirenti" else "Spektral Hafıza #$id"
                val sub = "Milat: Bölüm 91 • Kademe XI (91 - 99)"
                val rule = "Bu bölümlerdeki tüm geçmiş denemelerin ve hataların silinmez; yarı saydam canlı hayalet ışınları ve ruh parçacıkları olarak ekranda yaşar!"
                val hazard = "Kendi geçmişinin bıraktığı hayalet labirentine çarpmak geçmişin yükünü artırır ve +1 Yankı doğurur."
                val tip = "Kendi hayaletlerinin bıraktığı temiz koridorları zihninde haritalandır; geçmişinden ders çıkararak tek hamlede bitir."
                LevelRule(
                    levelId = id,
                    milestoneId = 91,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Önceki Hataların Birleşmesi",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🧬",
                    badgeColor = 0xFFA855F7,
                    accentColor = 0xFFE879F9,
                    headline = "Kural: Birleşik Hayalet Labirenti & Kendi Geçmişin",
                    summary = "Eski hatalar yaşayan bir labirenttir, geçmişini aşarak ilerle.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.CUMULATIVE_GHOST_TRAIL,
                    ruleKey = "CUMULATIVE_GHOSTS"
                )
            }
            else -> { // Level 100
                LevelRule(
                    levelId = 100,
                    milestoneId = 100,
                    isMajorMilestone = true,
                    tier = 12,
                    tierName = "Büyük Final: Omega Zirvesi",
                    ruleTitle = "Büyük Final: Tüm Mekaniklerin Birleşimi",
                    subtitle = "Büyük Milat: Bölüm 100 • Echo Evreninin Zirvesi",
                    icon = "👑",
                    badgeColor = 0xFFEF4444,
                    accentColor = 0xFFF87171,
                    headline = "NİHAİ SINAV: 100 Bölümün Tüm Kuralları Tek Bir Nefeste!",
                    summary = "Hareketli düğümler, görünmez hatlar, dönen evren, kilitli kapılar ve uyanmış Yankı Canavarı!",
                    mechanicRule = "Echo evreninin 100. ve en büyük zirvesi! Hareketli düğümler yörüngesinde süzülürken, çizdiğin ışınlar görünmez olur, ekran yavaşça döner, geçmiş hatalarının hayaletleri labirent oluşturur ve Yankı Canavarı DOMINION modunda gölgeni taklit eder!",
                    echoHazard = "En ufak bir geri dönüş, temas veya yön hatası uyanmış canavarın gazabına yol açar (+1 Yankı).",
                    proStrategy = "60 saniyelik sürenin her saniyesini soğukkanlılıkla kullan. 24 düğümlü Omega yayını tamamlayarak Efsanevi Şampiyon unvanını kazan!",
                    visualPreviewType = VisualPreviewType.OMEGA_NEXUS,
                    ruleKey = "OMEGA_SYNTHESIS"
                )
            }
        }
    }

    private fun buildEnglishRule(id: Int, tier: Int, isMajor: Boolean): LevelRule {
        return when (tier) {
            1 -> { // Levels 1..3
                val (title, sub, rule, hazard, tip, icon) = when (id) {
                    1 -> Tuple6(
                        "Continuous Flow",
                        "Milestone: Level 1 • Genesis",
                        "The player always starts strictly from Node 1. Connect all nodes with a single uninterrupted gesture without lifting your finger.",
                        "Lifting your finger, starting anywhere except Node 1, or backtracking to an earlier node triggers an Echo (+1).",
                        "Use the 5-second preview to scan your path before drawing. A 0-error run unlocks the Perfect Ghost run!",
                        "🎯"
                    )
                    2 -> Tuple6(
                        "Echo Footprint Hazard",
                        "Harmonic Foundation • Level 2",
                        "Mistakes do not vanish; they solidify into persistent red 'Echo Rays'. These red hazard lines become physical obstacles on subsequent attempts.",
                        "Colliding with your past red echo rays triggers fresh echoes and risks locking the board.",
                        "Strokes may pass through existing lines, but avoid retreating back onto red echo obstacles.",
                        "👣"
                    )
                    else -> Tuple6(
                        "Living Web Elasticity",
                        "Harmonic Foundation • Level 3",
                        "The web is alive! Connective rays stretch, breathe, and pull elastically toward your active touch cursor.",
                        "Jerky or abrupt finger movements can overshoot node hitboxes and register unintended retreats.",
                        "Glide your finger smoothly. Let the harmonic rhythm guide your stroke into each node.",
                        "🕸️"
                    )
                }
                LevelRule(
                    levelId = id,
                    milestoneId = 1,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Foundation: Living Web",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = icon,
                    badgeColor = 0xFF0284C7,
                    accentColor = 0xFF38BDF8,
                    headline = "Core Rule: Single Continuous Stroke",
                    summary = "Connect all nodes in one continuous line; avoid creating red echoes.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.PULSING_LIVING_WEB,
                    ruleKey = "CONTINUOUS_FLOW"
                )
            }
            2 -> { // Levels 4..10
                val title = if (id == 4) "Complex Web Lattice" else if (id == 7) "Elastic Bridge" else "Resonance Matrix #$id"
                val sub = "Milestone: Level 4 • Tier II (4 - 10)"
                val rule = "The web expands! Interconnected strands multiply, and previous rays vibrate with harmonic tension as your path grows."
                val hazard = "Branching density can trap you; entering a dead-end requires backtracking which incurs +1 Echo."
                val tip = "Trace internal bridge hubs early so the outer perimeter remains open for a clean exit."
                LevelRule(
                    levelId = id,
                    milestoneId = 4,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Complex Web",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🕸️",
                    badgeColor = 0xFF0EA5E9,
                    accentColor = 0xFF7DD3FC,
                    headline = "Rule: Dense Interconnections & Harmonic Tension",
                    summary = "Navigate dense thread lattices and plan your route ahead of time.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.COMPLEX_LATTICE,
                    ruleKey = "COMPLEX_WEB"
                )
            }
            3 -> { // Levels 11..20
                val title = if (id == 11) "Shifting Nodes" else if (id == 15) "Orbital Drift Velocity" else "Cosmic Current #$id"
                val sub = "Milestone: Level 11 • Tier III (11 - 20)"
                val rule = "Nodes are no longer stationary! They drift smoothly along orbital paths driven by cosmic currents. Rote memorization will fail; you must synchronize with their motion."
                val hazard = "Leading the finger into empty space where a node used to be breaks your path (+1 Echo)."
                val tip = "Watch the sinusoidal swing of each node; connect when they slow down at their orbital apexes."
                LevelRule(
                    levelId = id,
                    milestoneId = 11,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Shifting Nodes",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "⚡",
                    badgeColor = 0xFF8B5CF6,
                    accentColor = 0xFFA78BFA,
                    headline = "Rule: Dynamic Orbital Drift & Shifting Nodes",
                    summary = "Nodes drift continuously in space; match your timing to their orbit.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.ORBITAL_SHIFTING_NODES,
                    ruleKey = "SHIFTING_NODES"
                )
            }
            4 -> { // Levels 21..30
                val title = if (id == 21) "Invisible Lines" else if (id == 25) "Pulsar Stealth Beams" else "Stealth Photon #$id"
                val sub = "Milestone: Level 21 • Tier IV (21 - 30)"
                val rule = "Connected lines dissolve into invisibility seconds after being drawn! The beams remain physically active, but you must navigate using spatial memory."
                val hazard = "Colliding with an invisible earlier path segment or re-entering completed nodes incurs +1 Echo."
                val tip = "Maintain a vivid mental blueprint of the geometry you've already completed."
                LevelRule(
                    levelId = id,
                    milestoneId = 21,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Invisible Lines",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🌌",
                    badgeColor = 0xFFD97706,
                    accentColor = 0xFFFBBF24,
                    headline = "Rule: Stealth Photons & Spatial Memory",
                    summary = "Lines vanish from sight after connection; rely on your mental map.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.INVISIBLE_LINES_FADE,
                    ruleKey = "INVISIBLE_LINES"
                )
            }
            5 -> { // Levels 31..40
                val title = if (id == 31) "Decaying Nodes" else if (id == 35) "Temporal Dissolution Rush" else "Decay Ring #$id"
                val sub = "Milestone: Level 31 • Tier V (31 - 40)"
                val rule = "Selected nodes lose cosmic charge over time; their outer aura rings dissolve in a countdown. Visit decaying nodes before their energy fully evaporates!"
                val hazard = "If a decaying node collapses before your stroke touches it, the circuit breaks (+1 Echo)."
                val tip = "Prioritize nodes flashing red/orange on low energy; stabilize them during the first 15 seconds."
                LevelRule(
                    levelId = id,
                    milestoneId = 31,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Decaying Nodes",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "⏳",
                    badgeColor = 0xFFEC4899,
                    accentColor = 0xFFF472B6,
                    headline = "Rule: Temporal Dissolution & Rush Recovery",
                    summary = "Energy rings dissolve over time; visit endangered nodes early.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.DECAYING_RING,
                    ruleKey = "DECAYING_NODES"
                )
            }
            6 -> { // Levels 41..50
                val title = if (id == 41) "Wandering Echoes" else if (id == 45) "Drifting Past & Beast Growl" else "Floating Echo #$id"
                val sub = "Milestone: Level 41 • Tier VI (41 - 50)"
                val rule = "Red echo hazard walls do not stay in place! They drift slowly through space. Mistakes agitate the slumbering Echo Beast into heightened aggression."
                val hazard = "A drifting red echo beam that sweeps into your active finger stroke instantly triggers a collision (+1 Echo)."
                val tip = "Track the drift trajectory of floating echoes; slide through open channels when gaps widen."
                LevelRule(
                    levelId = id,
                    milestoneId = 41,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Wandering Echoes",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "👻",
                    badgeColor = 0xFF6366F1,
                    accentColor = 0xFF818CF8,
                    headline = "Rule: Floating Hazards & The Awakening Beast",
                    summary = "Red echo barriers drift across the canvas; avoid crossing paths.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.WANDERING_ECHO_DRIFT,
                    ruleKey = "WANDERING_ECHOES"
                )
            }
            7 -> { // Levels 51..60
                val title = if (id == 51) "Decoy Targets" else if (id == 55) "Phantom Mirage Minefield" else "Illusion Node #$id"
                val sub = "Milestone: Level 51 • Tier VII (51 - 60)"
                val rule = "Phantom decoy nodes populate the web! Real nodes pulse with steady harmonic resonance, while decoys exhibit stuttered, broken rings."
                val hazard = "Touching any phantom decoy node registers an immediate collision penalty (+1 Echo)."
                val tip = "True targets produce melodic chimes; ignore jittery decoy illusions."
                LevelRule(
                    levelId = id,
                    milestoneId = 51,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Decoy Targets",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🔮",
                    badgeColor = 0xFF14B8A6,
                    accentColor = 0xFF2DD4BF,
                    headline = "Rule: Decoy Illusions & Pattern Discrimination",
                    summary = "Distinguish genuine resonant nodes from phantom decoys.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.PHANTOM_DECOY,
                    ruleKey = "DECOY_TARGETS"
                )
            }
            8 -> { // Levels 61..70
                val title = if (id == 61) "Reverse Flow (One-Way Vectors)" else if (id == 65) "Vector Flow Highway" else "Counter Current #$id"
                val sub = "Milestone: Level 61 • Tier VIII (61 - 70)"
                val rule = "Directional vector arrows enforce strict one-way flow! You can only traverse these paths in the exact direction the arrow points."
                val hazard = "Drawing against the vector arrow direction blocks current and triggers +1 Echo."
                val tip = "Study one-way vectors before drawing to avoid entering a one-way street with no escape."
                LevelRule(
                    levelId = id,
                    milestoneId = 61,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Reverse Flow",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🔄",
                    badgeColor = 0xFF2563EB,
                    accentColor = 0xFF60A5FA,
                    headline = "Rule: One-Way Vector Conduits",
                    summary = "Travel strictly in the direction of vector arrows.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.VECTOR_REVERSE_FLOW,
                    ruleKey = "REVERSE_FLOW"
                )
            }
            9 -> { // Levels 71..80
                val title = if (id == 71) "Rotating Web" else if (id == 75) "Angular Vortex Spin" else "Gyro Matrix #$id"
                val sub = "Milestone: Level 71 • Tier IX (71 - 80)"
                val rule = "The entire coordinate grid and canvas rotate slowly during play! As your finger moves, the board angle dynamically turns."
                val hazard = "Failing to compensate for rotation causes touch coordinates to deviate, risking missed nodes (+1 Echo)."
                val tip = "Use the canvas center as your anchor and lead your stroke along the rotational curve."
                LevelRule(
                    levelId = id,
                    milestoneId = 71,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Rotating Web",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🌀",
                    badgeColor = 0xFFF59E0B,
                    accentColor = 0xFFFBBF24,
                    headline = "Rule: Angular Rotation & Centrifugal Touch",
                    summary = "Compensate for coordinate rotation as the web spins.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.ROTATING_CANVAS,
                    ruleKey = "ROTATING_WEB"
                )
            }
            10 -> { // Levels 81..90
                val title = if (id == 81) "Dual Entangled Web" else if (id == 85) "Interdimensional Key & Gate" else "Quantum Portal #$id"
                val sub = "Milestone: Level 81 • Tier X (81 - 90)"
                val rule = "Two intertwined dimensional planes (Alpha & Beta) co-exist! The golden Key in Dimension Alpha must be collected to unlock the Gate in Dimension Beta."
                val hazard = "Touching the locked Gate node before obtaining the matching Key breaks your path (+1 Echo)."
                val tip = "Use cross-dimensional bridge nodes to reach the Key first, saving the Gate for your final stretch."
                LevelRule(
                    levelId = id,
                    milestoneId = 81,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Dual Entangled Web",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "✨",
                    badgeColor = 0xFF8B5CF6,
                    accentColor = 0xFFC084FC,
                    headline = "Rule: Dual Dimensions & Quantum Keys",
                    summary = "Collect the Dimension Alpha Key to unlock the Dimension Beta Gate.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.DUAL_PORTAL,
                    ruleKey = "DUAL_ENTANGLED"
                )
            }
            11 -> { // Levels 91..99
                val title = if (id == 91) "Cumulative Ghost Maze" else if (id == 95) "Living Spirit Labyrinth" else "Spectral Memory #$id"
                val sub = "Milestone: Level 91 • Tier XI (91 - 99)"
                val rule = "All historical failed attempts from your past persist as translucent living ghost trails with floating spirit wisps, creating an evolving labyrinth!"
                val hazard = "Colliding with your past ghost labyrinth adds weight to your echoes (+1 Echo)."
                val tip = "Observe the clear corridors between your past ghost lines; let your past mistakes guide your winning path."
                LevelRule(
                    levelId = id,
                    milestoneId = 91,
                    isMajorMilestone = isMajor,
                    tier = tier,
                    tierName = "Cumulative Ghost Maze",
                    ruleTitle = title,
                    subtitle = sub,
                    icon = "🧬",
                    badgeColor = 0xFFA855F7,
                    accentColor = 0xFFE879F9,
                    headline = "Rule: Cumulative Ghost Maze & Past Mistakes",
                    summary = "Past failed attempts form an active ethereal maze to overcome.",
                    mechanicRule = rule,
                    echoHazard = hazard,
                    proStrategy = tip,
                    visualPreviewType = VisualPreviewType.CUMULATIVE_GHOST_TRAIL,
                    ruleKey = "CUMULATIVE_GHOSTS"
                )
            }
            else -> { // Level 100
                LevelRule(
                    levelId = 100,
                    milestoneId = 100,
                    isMajorMilestone = true,
                    tier = 12,
                    tierName = "Grand Finale: Omega Synthesis",
                    ruleTitle = "Grand Finale: All Rules United",
                    subtitle = "Apex Milestone: Level 100 • The Nexus of Echo",
                    icon = "👑",
                    badgeColor = 0xFFEF4444,
                    accentColor = 0xFFF87171,
                    headline = "THE ULTIMATE TRIAL: All 100 Level Rules in a Single Cosmic Breath!",
                    summary = "Shifting nodes, invisible rays, rotating canvas, gates, and the awakened Echo Beast!",
                    mechanicRule = "The monumental climax of the ECHO universe! Shifting nodes drift along orbits, connected lines vanish into stealth, the entire canvas rotates, cumulative ghosts weave an ethereal labyrinth, and the awakened Echo Beast mimics your path in DOMINION mode!",
                    echoHazard = "Any retreat, collision, or directional slip provokes the awakened Beast (+1 Echo).",
                    proStrategy = "Keep calm and steady. Complete all 24 nodes along the Omega Arch within 60 seconds to claim the Legendary Champion title!",
                    visualPreviewType = VisualPreviewType.OMEGA_NEXUS,
                    ruleKey = "OMEGA_SYNTHESIS"
                )
            }
        }
    }

    fun getAllMilestones(isTurkish: Boolean = false): List<LevelRule> {
        return MILESTONE_LEVELS.map { getRuleForLevel(it, isTurkish) }
    }

    fun getAll100Rules(isTurkish: Boolean = false): List<LevelRule> {
        return (1..100).map { getRuleForLevel(it, isTurkish) }
    }

    private data class Tuple6<A, B, C, D, E, F>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E,
        val sixth: F
    )
}
