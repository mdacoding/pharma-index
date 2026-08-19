package de.pharmaindex.config;

import de.pharmaindex.b2b.domain.Partner;
import de.pharmaindex.b2b.repo.PartnerRepository;
import de.pharmaindex.catalog.domain.ChangeType;
import de.pharmaindex.catalog.domain.DosageForm;
import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.catalog.domain.ProductStatus;
import de.pharmaindex.catalog.repo.ProductRepository;
import de.pharmaindex.catalog.service.ProductRevisionService;
import de.pharmaindex.matching.ProductMatcher;
import de.pharmaindex.pzn.PznChecksum;
import de.pharmaindex.quality.QualityEngine;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private final PartnerRepository partnerRepository;
    private final ProductRepository productRepository;
    private final ProductMatcher productMatcher;
    private final QualityEngine qualityEngine;
    private final ProductRevisionService revisionService;
    private final PharmaIndexProperties properties;

    public DemoDataSeeder(
            PartnerRepository partnerRepository,
            ProductRepository productRepository,
            ProductMatcher productMatcher,
            QualityEngine qualityEngine,
            ProductRevisionService revisionService,
            PharmaIndexProperties properties
    ) {
        this.partnerRepository = partnerRepository;
        this.productRepository = productRepository;
        this.productMatcher = productMatcher;
        this.qualityEngine = qualityEngine;
        this.revisionService = revisionService;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() > 0) {
            productMatcher.rebuild();
            return;
        }
        Partner partner = new Partner();
        partner.setName("Demo-Warenwirtschaft Apotheke Musterstadt");
        partner.setApiKey(properties.getSecurity().getDemoApiKey());
        partner.setActive(true);
        partnerRepository.save(partner);

        save(pzn("9900001"), "Aspirin 500 mg Tabletten", "Bayer Vital", "Acetylsalicylsäure", "N02BA01",
                "500 mg", DosageForm.TABLETTE, "20 Stück", "3.49", false, ProductStatus.ACTIVE);
        save(pzn("9900002"), "Paracetamol HEXAL 500 mg", "HEXAL", "Paracetamol", "N02BE01",
                "500 mg", DosageForm.TABLETTE, "20 Stück", "1.99", false, ProductStatus.ACTIVE);
        save(pzn("9900003"), "Ibuprofen AbZ 400 mg Filmtabletten", "AbZ Pharma", "Ibuprofen", "M01AE01",
                "400 mg", DosageForm.TABLETTE, "20 Stück", "4.29", false, ProductStatus.ACTIVE);
        save(pzn("9900004"), "Ibuprofen-ratiopharm 400 mg", "ratiopharm", "Ibuprofen", "M01AE01",
                "400 mg", DosageForm.TABLETTE, "50 Stück", "6.49", false, ProductStatus.ACTIVE);
        save(pzn("9900005"), "Omeprazol STADA 20 mg magensaftresistent", "STADA", "Omeprazol", "A02BC01",
                "20 mg", DosageForm.KAPSEL, "14 Stück", "7.80", false, ProductStatus.ACTIVE);
        save(pzn("9900006"), "Cetirizin AL 10 mg Filmtabletten", "ALIUD PHARMA", "Cetirizin", "R06AE07",
                "10 mg", DosageForm.TABLETTE, "20 Stück", "3.15", false, ProductStatus.ACTIVE);
        save(pzn("9900007"), "Diclofenac Heumann Gel 10 mg/g", "Heumann", "Diclofenac", "M02AA15",
                "10 mg/g", DosageForm.GEL, "100 g", "9.95", false, ProductStatus.ACTIVE);
        save(pzn("9900009"), "Amoxicillin ratiopharm 1000 mg", "ratiopharm", "Amoxicillin", "J01CA04",
                "1000 mg", DosageForm.TABLETTE, "20 Stück", "12.40", true, ProductStatus.ACTIVE);
        save(pzn("9900010"), "Metformin AbZ 1000 mg Filmtabletten", "AbZ Pharma", "Metformin", "A10BA02",
                "1000 mg", DosageForm.TABLETTE, "120 Stück", "8.20", true, ProductStatus.ACTIVE);
        save(pzn("9900011"), "Ramipril HEXAL 5 mg Tabletten", "HEXAL", "Ramipril", "C09AA05",
                "5 mg", DosageForm.TABLETTE, "100 Stück", "11.30", true, ProductStatus.ACTIVE);
        save(pzn("9900012"), "Salbutamol-ratiopharm Dosieraerosol", "ratiopharm", "Salbutamol", "R03AC02",
                "100 µg", DosageForm.SPRAY, "200 Hübe", "14.90", true, ProductStatus.ACTIVE);
        save(pzn("9900014"), "ACC akut 600 mg Brausetabletten", "Hexal", "Acetylcystein", "R05CB01",
                "600 mg", DosageForm.TABLETTE, "10 Stück", "8.45", false, ProductStatus.ACTIVE);
        save(pzn("9900021"), "Otriven gegen Schnupfen 0,1%", "GSK", "Xylometazolin", "R01AA07",
                "0,1 %", DosageForm.SPRAY, "10 ml", "4.79", false, ProductStatus.ACTIVE);
        save(pzn("9900022"), "Pantoprazol TAD 40 mg", "TAD Pharma", "Pantoprazol", "A02BC02",
                "40 mg", DosageForm.TABLETTE, "7 Stück", "6.10", false, ProductStatus.DISCONTINUED);

        save(pzn("9900023"), "Thomapyrin CLASSIC Tabletten", "Sanofi",
                "ASS, Paracetamol, Coffein", null, "250/200/50 mg", DosageForm.TABLETTE,
                "20 Stück", "5.49", false, ProductStatus.ACTIVE);
        save(pzn("9900024"), "Immunsuppressivum Demo 5 mg", "DemoPharm",
                "Tacrolimus", "L04AD02", "5 mg", DosageForm.KAPSEL, "30 Stück", "389.00", true, ProductStatus.ACTIVE);
        save(pzn("9900025"), "NASENSPRAY XYLO 0,1%", "Genericis",
                "Xylometazolin", "R01AA07", "1 mg/ml", DosageForm.SPRAY, "10 ml", "3.20", false, ProductStatus.DRAFT);
        save(pzn("9900030"), "Simvastatin AbZ 20 mg Filmtabletten", "AbZ Pharma", "Simvastatin", "C10AA01",
                "20 mg", DosageForm.TABLETTE, "100 Stück", "9.80", true, ProductStatus.ACTIVE);
        save(pzn("9900031"), "Amlodipin HEXAL 5 mg Tabletten", "HEXAL", "Amlodipin", "C08CA01",
                "5 mg", DosageForm.TABLETTE, "100 Stück", "7.40", true, ProductStatus.ACTIVE);
        save(pzn("9900032"), "L-Thyroxin HEXAL 75 µg", "HEXAL", "Levothyroxin", "H03AA01",
                "75 µg", DosageForm.TABLETTE, "100 Stück", "12.90", true, ProductStatus.ACTIVE);
        save(pzn("9900036"), "Prednisolon acis 5 mg Tabletten", "acis", "Prednisolon", "H02AB06",
                "5 mg", DosageForm.TABLETTE, "20 Stück", "6.50", true, ProductStatus.ACTIVE);
        save(pzn("9900035"), "Bisoprolol-ratiopharm 5 mg", "ratiopharm", "Bisoprolol", "C07AB07",
                "5 mg", DosageForm.TABLETTE, "100 Stück", "8.10", true, ProductStatus.ACTIVE);

        Product invalidPzn = new Product();
        invalidPzn.setPzn("11111111");
        invalidPzn.setName("Fehlerhafter Import: Schmerzmittel X");
        invalidPzn.setManufacturer("Unbekannt");
        invalidPzn.setActiveIngredient("Ibuprofen");
        invalidPzn.setForm(DosageForm.TABLETTE);
        invalidPzn.setStatus(ProductStatus.DRAFT);
        invalidPzn.setPrescriptionRequired(false);
        productRepository.save(invalidPzn);

        productMatcher.rebuild();
        productRepository.findAll().forEach(product -> {
            revisionService.record(product, ChangeType.CREATED);
            qualityEngine.scan(product);
        });
    }

    private Product save(
            String pzn,
            String name,
            String manufacturer,
            String ingredient,
            String atc,
            String strength,
            DosageForm form,
            String pack,
            String price,
            boolean rx,
            ProductStatus status
    ) {
        Product product = new Product();
        product.setPzn(pzn);
        product.setName(name);
        product.setManufacturer(manufacturer);
        product.setActiveIngredient(ingredient);
        product.setAtcCode(atc);
        product.setStrength(strength);
        product.setForm(form);
        product.setPackageSize(pack);
        product.setPharmacyPrice(new BigDecimal(price));
        product.setPrescriptionRequired(rx);
        product.setStatus(status);
        return productRepository.save(product);
    }

    private static String pzn(String sevenDigits) {
        return PznChecksum.withCheckDigit(sevenDigits);
    }
}
