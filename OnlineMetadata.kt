var overview = candidate.overview
        var overviewLang = lang
        val currentTitle = candidate.names.firstOrNull { it.isNotBlank() }.orEmpty()
        val origLang = candidate.originalLanguage.substringBefore('-').lowercase()
        val uiLang = lang.substringBefore('-').lowercase()
        // Nếu phim không phải ngôn ngữ giao diện hiện tại (vd: phim không phải tiếng Việt)
        // VÀ tên trả về trùng với tên gốc (nghĩa là TMDB chưa có bản dịch cho ngôn ngữ này)
        val untitledInUiLang = lang != ENGLISH &&
            origLang.isNotBlank() &&
            origLang != uiLang &&
            candidate.names.size >= 2 &&
            currentTitle.equals(candidate.names.last(), ignoreCase = true)

        if ((overview.isBlank() || untitledInUiLang) && lang != ENGLISH) {
            val english = inEnglish?.let { MetaReply.Ok(it) }
                ?: retrying { if (film) tmdb.movie(key, candidate.id, ENGLISH) else tmdb.tv(key, candidate.id, ENGLISH) }
            if (english is MetaReply.Ok) {
                if (overview.isBlank()) {
                    overview = english.value.overview
                    overviewLang = ENGLISH
                }
                if (untitledInUiLang) {
                    val enTitle = english.value.names.firstOrNull { it.isNotBlank() }
                    if (!enTitle.isNullOrBlank()) {
                        candidate = candidate.copy(names = listOf(enTitle) + candidate.names)
                    }
                }
            }
        }
        return Outcome.Found(info(query, candidate, film, poster, overview, overviewLang))
