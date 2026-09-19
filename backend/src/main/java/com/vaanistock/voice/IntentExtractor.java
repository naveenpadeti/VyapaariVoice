package com.vaanistock.voice;

import com.vaanistock.category.Category;
import com.vaanistock.category.CategoryRepository;
import com.vaanistock.product.Product;
import com.vaanistock.product.ProductRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IntentExtractor {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private static final Set<String> KNOWN_UNITS = Set.of(
            "bag", "bags", "kg", "kgs", "kilo", "kilos", "kilogram", "kilograms",
            "box", "boxes", "packet", "packets", "pkt", "pkts",
            "bottle", "bottles", "liter", "liters", "litre", "litres", "l",
            "piece", "pieces", "pc", "pcs", "carton", "cartons",
            "dozen", "dozens", "crate", "crates", "bundle", "bundles",
            "quintal", "quintals", "ton", "tons"
    );

    public IntentExtractor(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ParsedVoiceCommand parse(Long businessId, String rawText, String languageHint) {
        ParsedVoiceCommand cmd = new ParsedVoiceCommand();
        if (rawText == null || rawText.trim().isEmpty()) {
            cmd.setIntent(VoiceIntent.UNKNOWN);
            return cmd;
        }

        String text = rawText.trim();
        cmd.setRawTranscript(text);

        String detectedLang = detectLanguage(text, languageHint);
        cmd.setDetectedLanguage(detectedLang);

        String lower = text.toLowerCase();

        // Load catalog for this business to assist entity resolution
        List<Product> products = productRepository.findByBusinessIdOrderByNameAsc(businessId);
        List<Category> categories = categoryRepository.findByBusinessIdOrderByNameAsc(businessId);

        // 1. Identify category match
        for (Category cat : categories) {
            if (lower.contains(cat.getName().toLowerCase())) {
                cmd.setCategoryName(cat.getName());
                break;
            }
        }

        // 2. Extract numeric quantity
        BigDecimal extractedQty = extractQuantity(lower);
        cmd.setQuantity(extractedQty);

        // 3. Extract unit
        String extractedUnit = extractUnit(lower);
        cmd.setUnit(extractedUnit);

        // 4. Identify product from catalog
        Product matchedProduct = matchProductFromCatalog(products, lower);
        if (matchedProduct != null) {
            cmd.setProductName(matchedProduct.getName());
            if (cmd.getUnit() == null || cmd.getUnit().isBlank()) {
                cmd.setUnit(matchedProduct.getUnit());
            }
        } else {
            // Fallback product name extraction
            String guessedProduct = guessProductName(lower, extractedUnit);
            if (guessedProduct != null) {
                cmd.setProductName(guessedProduct);
            }
        }

        // 5. Determine Intent
        VoiceIntent intent = determineIntent(lower, cmd);
        cmd.setIntent(intent);

        return cmd;
    }

    private VoiceIntent determineIntent(String lower, ParsedVoiceCommand cmd) {
        // A. INVENTORY SUMMARY
        if (lower.contains("inventory summary") || lower.contains("shop summary") ||
            lower.contains("summary cheppu") || lower.contains("summary batao") ||
            lower.contains("na shop inventory") || lower.contains("shop ka summary") ||
            lower.contains("total summary") || lower.contains("overall summary") ||
            (lower.contains("summary") && !lower.contains("rice") && !lower.contains("sugar"))) {
            return VoiceIntent.INVENTORY_SUMMARY;
        }

        // B. REORDER RECOMMENDATION
        if (lower.contains("refill cheyyala") || lower.contains("refill kavala") ||
            lower.contains("refill cheyyali") || lower.contains("order cheyyala") ||
            lower.contains("teppinchala") || lower.contains("saripothunda") || lower.contains("saripothada") ||
            lower.contains("reorder") || lower.contains("refill karna") || lower.contains("mangwana hai") ||
            lower.contains("should i refill") || lower.contains("should i reorder") ||
            lower.contains("need to refill") || lower.contains("recommend refill")) {
            return VoiceIntent.REORDER_RECOMMENDATION;
        }

        // C. LOW STOCK
        if (lower.contains("low stock") || lower.contains("which items are low") ||
            lower.contains("which products are low") || lower.contains("low unnai") ||
            lower.contains("takkuva unnai") || lower.contains("takkuva unna") ||
            lower.contains("kam stock") || lower.contains("khatam hone") ||
            lower.contains("running out") || lower.contains("items low")) {
            return VoiceIntent.LOW_STOCK;
        }

        // D. FAST MOVING
        if (lower.contains("fast moving") || lower.contains("fast selling") ||
            lower.contains("top selling") || lower.contains("best seller") ||
            lower.contains("veganga") || lower.contains("sabse zyada bikne")) {
            return VoiceIntent.FAST_MOVING;
        }

        // E. SLOW MOVING
        if (lower.contains("slow moving") || lower.contains("least selling") ||
            lower.contains("mella ga") || lower.contains("kam bikne")) {
            return VoiceIntent.SLOW_MOVING;
        }

        // F. NO RECENT SALES
        if (lower.contains("no sales") || lower.contains("no recent sales") ||
            lower.contains("sales leni") || lower.contains("ammudu kaani") ||
            lower.contains("stale products") || lower.contains("bina sales")) {
            return VoiceIntent.NO_RECENT_SALES;
        }

        // G. CHECK CATEGORY
        if ((lower.contains("category") || lower.contains(" lo em ") || lower.contains(" me kya ")) &&
            cmd.getCategoryName() != null) {
            return VoiceIntent.CHECK_CATEGORY;
        }

        // H. ADD STOCK
        if (lower.contains("vachayi") || lower.contains("vachindi") || lower.contains("vachai") ||
            lower.contains("add cheyyi") || lower.contains("add karo") || lower.contains("kalupumu") ||
            lower.contains("pettumu") || lower.contains("vesamu") || lower.contains("cherchamu") ||
            lower.contains("aaya") || lower.contains("aayi") || lower.contains("aaye") ||
            lower.contains("mila") || lower.contains("received") || lower.contains("add ") ||
            lower.startsWith("add ") || lower.contains("added") || lower.contains("incoming")) {
            return VoiceIntent.ADD_STOCK;
        }

        // I. REMOVE STOCK (Sale or outgoing)
        if (lower.contains("ammamu") || lower.contains("ammina") || lower.contains("ammadam") ||
            lower.contains("becha") || lower.contains("bech diya") || lower.contains("sold") ||
            lower.contains("remove") || lower.contains("theesi") || lower.contains("theeseyyi") ||
            lower.contains("kam karo") || lower.contains("nikalo") || lower.contains("hatao") ||
            lower.contains("ichamu") || lower.contains("vellipoyindi") || lower.contains("outgoing")) {
            return VoiceIntent.REMOVE_STOCK;
        }

        // J. CHECK STOCK
        if (lower.contains("stock entha undi") || lower.contains("stock entha") ||
            lower.contains("entha undi") || lower.contains("stock undi") || lower.contains("stock undha") ||
            lower.contains("kitna stock") || lower.contains("kitna hai") || lower.contains("stock kitna") ||
            lower.contains("how many") || lower.contains("how much") || lower.contains("check stock") ||
            lower.contains("available") || lower.contains("stock of") ||
            (cmd.getProductName() != null && cmd.getQuantity() == null)) {
            return VoiceIntent.CHECK_STOCK;
        }

        // K. SEARCH PRODUCT
        if (lower.contains("search") || lower.contains("find") || lower.contains("vetuku") || lower.contains("khojo")) {
            return VoiceIntent.SEARCH_PRODUCT;
        }

        // Fallback: If quantity exists with a product -> default to ADD_STOCK or CHECK_STOCK
        if (cmd.getProductName() != null && cmd.getQuantity() != null) {
            return VoiceIntent.ADD_STOCK;
        }

        return VoiceIntent.UNKNOWN;
    }

    private Product matchProductFromCatalog(List<Product> products, String lower) {
        // 1. Exact or whole token match
        for (Product p : products) {
            String pName = p.getName().toLowerCase();
            if (Pattern.compile("\\b" + Pattern.quote(pName) + "\\b").matcher(lower).find()) {
                return p;
            }
        }

        // 2. First-word match (e.g. "Rice" for "Rice 25kg")
        for (Product p : products) {
            String firstWord = p.getName().split("\\s+")[0].toLowerCase();
            if (firstWord.length() >= 3 && Pattern.compile("\\b" + Pattern.quote(firstWord) + "\\b").matcher(lower).find()) {
                return p;
            }
        }

        // 3. Substring match
        for (Product p : products) {
            String pName = p.getName().toLowerCase();
            if (lower.contains(pName) || pName.contains(lower)) {
                return p;
            }
        }

        return null;
    }

    private BigDecimal extractQuantity(String lower) {
        // Match numbers like 20, 20.5, 5
        Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(lower);
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1));
            } catch (Exception ignored) {}
        }

        // Match common words for numbers in English/Telugu/Hindi
        Map<String, BigDecimal> wordNumbers = Map.ofEntries(
                Map.entry("one", BigDecimal.ONE), Map.entry("okati", BigDecimal.ONE), Map.entry("ek", BigDecimal.ONE),
                Map.entry("two", BigDecimal.valueOf(2)), Map.entry("rendu", BigDecimal.valueOf(2)), Map.entry("do", BigDecimal.valueOf(2)),
                Map.entry("three", BigDecimal.valueOf(3)), Map.entry("moodu", BigDecimal.valueOf(3)), Map.entry("teen", BigDecimal.valueOf(3)),
                Map.entry("four", BigDecimal.valueOf(4)), Map.entry("naalugu", BigDecimal.valueOf(4)), Map.entry("chaar", BigDecimal.valueOf(4)),
                Map.entry("five", BigDecimal.valueOf(5)), Map.entry("aidhu", BigDecimal.valueOf(5)), Map.entry("paanch", BigDecimal.valueOf(5)),
                Map.entry("ten", BigDecimal.valueOf(10)), Map.entry("padi", BigDecimal.valueOf(10)), Map.entry("das", BigDecimal.valueOf(10)),
                Map.entry("fifteen", BigDecimal.valueOf(15)), Map.entry("padiheenu", BigDecimal.valueOf(15)), Map.entry("pandrah", BigDecimal.valueOf(15)),
                Map.entry("twenty", BigDecimal.valueOf(20)), Map.entry("iravai", BigDecimal.valueOf(20)), Map.entry("bees", BigDecimal.valueOf(20)),
                Map.entry("twenty five", BigDecimal.valueOf(25)), Map.entry("iravai aidhu", BigDecimal.valueOf(25)), Map.entry("pachees", BigDecimal.valueOf(25)),
                Map.entry("fifty", BigDecimal.valueOf(50)), Map.entry("yabhai", BigDecimal.valueOf(50)), Map.entry("pachaas", BigDecimal.valueOf(50))
        );

        for (Map.Entry<String, BigDecimal> entry : wordNumbers.entrySet()) {
            if (Pattern.compile("\\b" + Pattern.quote(entry.getKey()) + "\\b").matcher(lower).find()) {
                return entry.getValue();
            }
        }

        return null;
    }

    private String extractUnit(String lower) {
        for (String unit : KNOWN_UNITS) {
            if (Pattern.compile("\\b" + Pattern.quote(unit) + "\\b").matcher(lower).find()) {
                return normalizeUnit(unit);
            }
        }
        return null;
    }

    private String normalizeUnit(String unit) {
        String u = unit.toLowerCase();
        if (u.equals("bags") || u.equals("bag")) return "bags";
        if (u.equals("kgs") || u.equals("kilo") || u.equals("kilos") || u.equals("kilogram") || u.equals("kilograms") || u.equals("kg")) return "kg";
        if (u.equals("boxes") || u.equals("box")) return "boxes";
        if (u.equals("packets") || u.equals("packet") || u.equals("pkts") || u.equals("pkt")) return "packets";
        if (u.equals("bottles") || u.equals("bottle")) return "bottles";
        if (u.equals("liters") || u.equals("litres") || u.equals("litre") || u.equals("liter") || u.equals("l")) return "liters";
        if (u.equals("pieces") || u.equals("piece") || u.equals("pcs") || u.equals("pc")) return "pieces";
        if (u.equals("cartons") || u.equals("carton")) return "cartons";
        return u;
    }

    private String guessProductName(String lower, String extractedUnit) {
        // Strip out known verbs, stop words, numbers, units
        String cleaned = lower
                .replaceAll("\\b(\\d+(\\.\\d+)?)\\b", "")
                .replaceAll("\\b(bags?|kgs?|boxes?|packets?|bottles?|liters?|pieces?)\\b", "")
                .replaceAll("\\b(vachayi|vachindi|ammamu|ammina|stock|entha|undi|undha|cheyyala|lo|em|unnai|kavala)\\b", "")
                .replaceAll("\\b(aaya|aayi|becha|kitna|hai|karo|batao|kya|ke|mein)\\b", "")
                .replaceAll("\\b(add|remove|sold|how|many|much|check|the|is|are|of|to|in|please)\\b", "")
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .trim();

        if (cleaned.length() >= 2) {
            String[] tokens = cleaned.split("\\s+");
            if (tokens.length > 0 && !tokens[0].isBlank()) {
                // Capitalize first letter
                return tokens[0].substring(0, 1).toUpperCase() + tokens[0].substring(1);
            }
        }
        return null;
    }

    public String detectLanguage(String text, String hint) {
        if (hint != null && (hint.equals("te") || hint.equals("hi") || hint.equals("en"))) {
            return hint;
        }

        // Check for Telugu script (\u0C00-\u0C7F)
        for (char c : text.toCharArray()) {
            if (c >= '\u0C00' && c <= '\u0C7F') return "te";
        }
        // Check for Devanagari script (\u0900-\u097F)
        for (char c : text.toCharArray()) {
            if (c >= '\u0900' && c <= '\u097F') return "hi";
        }

        String lower = text.toLowerCase();
        // Check Telugu transliterated markers
        if (lower.contains("vachayi") || lower.contains("ammamu") || lower.contains("entha") ||
            lower.contains("undi") || lower.contains("cheyyala") || lower.contains("unnai") ||
            lower.contains("saripothunda") || lower.contains("teppinchala") || lower.contains("cheppu")) {
            return "te";
        }

        // Check Hindi transliterated markers
        if (lower.contains("kitna") || lower.contains("becha") || lower.contains("hai") ||
            lower.contains("karo") || lower.contains("batao") || lower.contains("kaunse") ||
            lower.contains("mangwana") || lower.contains("bikne")) {
            return "hi";
        }

        return "en";
    }
}
