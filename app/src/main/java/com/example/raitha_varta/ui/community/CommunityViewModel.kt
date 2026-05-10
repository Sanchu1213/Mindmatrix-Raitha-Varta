package com.example.raitha_varta.ui.community

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorLocation: String,
    val question: String,
    val image: Bitmap? = null,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val replies: List<CommunityReply> = emptyList(),
    val likes: Int = 0,
    val tag: String = "General",
    val isLiked: Boolean = false
)

data class CommunityReply(
    val authorName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isExpert: Boolean = false
)

@HiltViewModel
class CommunityViewModel @Inject constructor() : ViewModel() {

    private val _posts = MutableStateFlow(getMockPosts())
    val posts = _posts.asStateFlow()

    private val _selectedPost = MutableStateFlow<CommunityPost?>(null)
    val selectedPost = _selectedPost.asStateFlow()

    fun addPost(question: String, authorName: String, location: String, tag: String, image: Bitmap?) {
        val newPost = CommunityPost(
            id = System.currentTimeMillis().toString(),
            authorName = authorName,
            authorLocation = location,
            question = question,
            image = image,
            timestamp = System.currentTimeMillis(),
            tag = tag,
            replies = emptyList(),
            likes = 0
        )
        _posts.value = listOf(newPost) + _posts.value
    }

    fun toggleLike(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(
                    likes = if (post.isLiked) post.likes - 1 else post.likes + 1,
                    isLiked = !post.isLiked
                )
            } else post
        }
    }

    fun selectPost(post: CommunityPost) { _selectedPost.value = post }
    fun clearSelectedPost() { _selectedPost.value = null }

    fun addReply(postId: String, replyText: String, authorName: String) {
        val reply = CommunityReply(
            authorName = authorName,
            text = replyText,
            timestamp = System.currentTimeMillis(),
            isExpert = false
        )
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(replies = post.replies + reply)
            } else post
        }
        _selectedPost.value = _posts.value.find { it.id == postId }
    }

    private fun getMockPosts() = listOf(
        CommunityPost(
            id = "1",
            authorName = "Ramesh Gowda",
            authorLocation = "Hassan, Karnataka ||| ಹಾಸನ, ಕರ್ನಾಟಕ",
            question = "My tomato leaves are turning yellow from the bottom. Applied fertilizer last week. Is this nitrogen deficiency or some disease? Please help! 🍅 ||| ನನ್ನ ಟೊಮ್ಯಾಟೊ ಎಲೆಗಳು ಕೆಳಗಿನಿಂದ ಹಳದಿ ಬಣ್ಣಕ್ಕೆ ತಿರುಗುತ್ತಿವೆ. ಕಳೆದ ವಾರ ಗೊಬ್ಬರ ಹಾಕಿದ್ದೇನೆ. ಇದು ಸಾರಜನಕದ ಕೊರತೆಯೇ ಅಥವಾ ಯಾವುದಾದರೂ ರೋಗವೇ? ದಯವಿಟ್ಟು ಸಹಾಯ ಮಾಡಿ! 🍅",
            timestamp = System.currentTimeMillis() - 3600000,
            tag = "Disease",
            likes = 12,
            replies = listOf(
                CommunityReply("Dr. KVK Expert", "This looks like early blight (Alternaria). Apply Mancozeb 2g/litre spray. Ensure proper spacing for air circulation. ||| ಇದು ಮುಂಗಾರು ಎಲೆ ಚುಕ್ಕೆ ರೋಗದಂತೆ (Alternaria) ಕಾಣುತ್ತದೆ. ಮ್ಯಾಂಕೋಜೆಬ್ 2 ಗ್ರಾಂ/ಲೀಟರ್ ಸಿಂಪಡಿಸಿ. ಗಾಳಿ ಸಂಚಾರಕ್ಕಾಗಿ ಸರಿಯಾದ ಅಂತರವನ್ನು ಕಾಯ್ದುಕೊಳ್ಳಿ.", isExpert = true),
                CommunityReply("Suresh Farmer", "Same issue I had last season. Copper fungicide worked well for me. ||| ಕಳೆದ ಋತುವಿನಲ್ಲಿ ನನಗೂ ಇದೇ ಸಮಸ್ಯೆ ಇತ್ತು. ತಾಮ್ರದ ಶಿಲೀಂಧ್ರನಾಶಕ ನನ್ನ ಮಟ್ಟಿಗೆ ಚೆನ್ನಾಗಿ ಕೆಲಸ ಮಾಡಿದೆ.", isExpert = false)
            )
        ),
        CommunityPost(
            id = "2",
            authorName = "Lakshmi Devi",
            authorLocation = "Tumkur, Karnataka ||| ತುಮಕೂರು, ಕರ್ನಾಟಕ",
            question = "What is the best variety of ragi for my black cotton soil? The rainfall here is about 800mm. Looking for high yield variety. ||| ನನ್ನ ಕಪ್ಪು ಮಣ್ಣಿಗೆ ಉತ್ತಮವಾದ ರಾಗಿ ತಳಿ ಯಾವುದು? ಇಲ್ಲಿನ ಮಳೆ ಸುಮಾರು 800 ಮಿ.ಮೀ. ಅಧಿಕ ಇಳುವರಿ ನೀಡುವ ತಳಿಗಾಗಿ ಹುಡುಕುತ್ತಿದ್ದೇನೆ.",
            timestamp = System.currentTimeMillis() - 7200000,
            tag = "Seeds",
            likes = 8,
            replies = listOf(
                CommunityReply("ICAR Scientist", "For black cotton soil with 800mm rainfall, GPU-28 and MR-6 are excellent. GPU-28 yields 25-30 q/acre. ||| 800 ಮಿ.ಮೀ ಮಳೆಯಿರುವ ಕಪ್ಪು ಮಣ್ಣಿಗೆ GPU-28 ಮತ್ತು MR-6 ಅತ್ಯುತ್ತಮವಾಗಿವೆ. GPU-28 ಎಕರೆಗೆ 25-30 ಕ್ವಿಂಟಾಲ್ ಇಳುವರಿ ನೀಡುತ್ತದೆ.", isExpert = true)
            )
        ),
        CommunityPost(
            id = "3",
            authorName = "Venkatesh Patil",
            authorLocation = "Dharwad, Karnataka ||| ಧಾರವಾಡ, ಕರ್ನಾಟಕ",
            question = "Is it good to grow maize after sugarcane? My field has been under sugarcane for 3 years. Soil pH is 7.2. ||| ಕಬ್ಬಿನ ನಂತರ ಮೆಕ್ಕೆಜೋಳ ಬೆಳೆಯುವುದು ಉತ್ತಮವೇ? ನನ್ನ ಹೊಲದಲ್ಲಿ 3 ವರ್ಷಗಳಿಂದ ಕಬ್ಬು ಬೆಳೆಯಲಾಗುತ್ತಿದೆ. ಮಣ್ಣಿನ pH 7.2 ಆಗಿದೆ.",
            timestamp = System.currentTimeMillis() - 14400000,
            tag = "Soil",
            likes = 5,
            replies = listOf(
                CommunityReply("Agri Extension", "Yes, maize is a good crop after sugarcane. Add green manure (dhaincha) to replenish organic matter. Ensure good potassium supplementation. ||| ಹೌದು, ಕಬ್ಬಿನ ನಂತರ ಮೆಕ್ಕೆಜೋಳ ಒಳ್ಳೆಯ ಬೆಳೆ. ಸಾವಯವ ಪದಾರ್ಥಗಳನ್ನು ಮರುಪೂರಣ ಮಾಡಲು ಹಸಿರು ಗೊಬ್ಬರವನ್ನು (ಧೈಂಚಾ) ಸೇರಿಸಿ. ಸರಿಯಾದ ಪೊಟ್ಯಾಸಿಯಮ್ ಪೂರೈಕೆಯನ್ನು ಖಚಿತಪಡಿಸಿಕೊಳ್ಳಿ.", isExpert = true)
            )
        ),
        CommunityPost(
            id = "4",
            authorName = "Parvathi Nair",
            authorLocation = "Mysuru, Karnataka ||| ಮೈಸೂರು, ಕರ್ನಾಟಕ",
            question = "Got my soil test report. NPK is 180:18:140 kg/ha. What fertilizer dose should I follow for paddy cultivation? This is Kharif season. ||| ನನ್ನ ಮಣ್ಣಿನ ಪರೀಕ್ಷಾ ವರದಿ ಬಂದಿದೆ. NPK 180:18:140 ಕೆಜಿ/ಹೆಕ್ಟೇರ್ ಆಗಿದೆ. ಭತ್ತದ ಬೇಸಾಯಕ್ಕೆ ನಾನು ಯಾವ ಗೊಬ್ಬರದ ಪ್ರಮಾಣವನ್ನು ಅನುಸರಿಸಬೇಕು? ಇದು ಖಾರಿಫ್ ಹಂಗಾಮು.",
            timestamp = System.currentTimeMillis() - 86400000,
            tag = "Fertilizer",
            likes = 21,
            replies = listOf(
                CommunityReply("KVK Agronomist", "For Kharif paddy with your NPK: Apply 100kg Urea, 150kg SSP, 50kg MOP per acre in split doses. First dose at transplanting, second at panicle initiation. ||| ನಿಮ್ಮ NPK ಯೊಂದಿಗೆ ಖಾರಿಫ್ ಭತ್ತಕ್ಕೆ: ಎಕರೆಗೆ 100 ಕೆಜಿ ಯೂರಿಯಾ, 150 ಕೆಜಿ ಎಸ್‌ಎಸ್‌ಪಿ, 50 ಕೆಜಿ ಎಂಒಪಿಯನ್ನು ವಿಭಜಿತ ಪ್ರಮಾಣದಲ್ಲಿ ಅನ್ವಯಿಸಿ. ಮೊದಲ ಡೋಸ್ ನಾಟಿ ಸಮಯದಲ್ಲಿ, ಎರಡನೆಯದು ಹೂಗೊಂಚಲು ಆರಂಭದ ಹಂತದಲ್ಲಿ.", isExpert = true),
                CommunityReply("Mahesh Kumar", "Also add 2.5 tonnes of FYM before transplanting for best results. ||| ಉತ್ತಮ ಫಲಿತಾಂಶಕ್ಕಾಗಿ ನಾಟಿ ಮಾಡುವ ಮೊದಲು 2.5 ಟನ್ ಕೊಟ್ಟಿಗೆ ಗೊಬ್ಬರವನ್ನು (FYM) ಸೇರಿಸಿ.", isExpert = false)
            )
        ),
        CommunityPost(
            id = "5",
            authorName = "Basavaraj Hegde",
            authorLocation = "Haveri, Karnataka ||| ಹಾವೇರಿ, ಕರ್ನಾಟಕ",
            question = "Which is the best time to sow sunflower in Karnataka? Also what spacing is recommended for KBSH-44 hybrid? ||| ಕರ್ನಾಟಕದಲ್ಲಿ ಸೂರ್ಯಕಾಂತಿ ಬಿತ್ತನೆ ಮಾಡಲು ಉತ್ತಮ ಸಮಯ ಯಾವುದು? ಹಾಗೆಯೇ KBSH-44 ಹೈಬ್ರಿಡ್‌ಗೆ ಶಿಫಾರಸು ಮಾಡಲಾದ ಅಂತರ ಎಷ್ಟು?",
            timestamp = System.currentTimeMillis() - 172800000,
            tag = "Sowing",
            likes = 15,
            replies = emptyList()
        )
    )
}
